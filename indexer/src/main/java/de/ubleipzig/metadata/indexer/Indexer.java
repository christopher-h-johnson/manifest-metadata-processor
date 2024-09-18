/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ubleipzig.metadata.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.ContentList;
import de.ubleipzig.metadata.templates.MetadataMap;
import de.ubleipzig.metadata.templates.atomic.AnnotationBodyAtom;
import de.ubleipzig.metadata.templates.atomic.AtomList;
import de.ubleipzig.metadata.templates.indexer.ElasticCreate;
import de.ubleipzig.metadata.templates.indexer.ElasticDocumentObject;
import de.ubleipzig.metadata.templates.indexer.ElasticIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.rdf.api.IRI;
import org.apache.jena.commonsrdf.JenaRDF;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.ubleipzig.metadata.indexer.Constants.*;
import static de.ubleipzig.metadata.processor.JsonSerializer.MAPPER;
import static de.ubleipzig.metadata.processor.QueryUtils.readFile;

@Slf4j
public class Indexer {
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();

    public Indexer() {
    }

    private static String getDocumentId() {
        return UUID.randomUUID().toString();
    }

    public ElasticIndex createIndex(String indexName, String type, String id) {
        final ElasticDocumentObject index = new ElasticDocumentObject();
        index.setIndex(indexName);
        index.setType(type);
        index.setId(id);
        final ElasticIndex i = new ElasticIndex();
        i.setIndex(index);
        return i;
    }

    public ElasticCreate createDocument(String indexName, String id) {
        final ElasticDocumentObject doc = new ElasticDocumentObject();
        doc.setIndex(indexName);
        doc.setId(id);
        final ElasticCreate create = new ElasticCreate();
        create.setCreate(doc);
        return create;
    }

    public void indexJson(String baseUrl, String documentId, String indexName, String indexType, String json) {
        final IRI identifier = rdf.createIRI(baseUrl + indexName + indexType + "/" + documentId);
        InputStream is = new ByteArrayInputStream(json.getBytes());
        try {
            client.put(identifier, is, "application/json");
        } catch (LdpClientException e) {
            log.error(e.getMessage());
        }
    }

    public void createIndexMapping(String baseUrl, InputStream mapping) {
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode jsonNode = MAPPER.readValue(readFile(mapping), JsonNode.class);
            sb.append(jsonNode.toString());
            System.out.println(sb.toString());
            InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            String username = "admin";
            String password = "OpenSearch1!";
            String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            final String token = "Basic " + encoded;
            client.putWithAuth(rdf.createIRI(baseUrl), is, "application/json", token);
        } catch (LdpClientException | IOException e) {
            log.error(e.getMessage());
        }
    }

    public MetadataMap getMetadataMap(IRI iri) {
        try {
            final String extractorService = "http://localhost:9098/extractor?type=extract&m=";
            final String extractorRequest = extractorService + iri.getIRIString();
            final IRI req = rdf.createIRI(extractorRequest);
            final HttpResponse<?> res = client.getResponse(req);
            if (res.statusCode() == 200) {
                final String json = res.body().toString();
                return MAPPER.readValue(json, new TypeReference<MetadataMap>() {
                });
            }
        } catch (LdpClientException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    public void putJsonAtomsElasticBulk(IRI iri, String indexName) {
        final Indexer indexer = new Indexer();
        final String bulkUri = elasticSearchHost + bulkContext;

        final StringBuffer sb = new StringBuffer();
        try {
            final HttpResponse<?> res = client.getResponse(iri);
            if (res.statusCode() == 200) {
                final String jsonList = res.body().toString();
                final AtomList atomList = MAPPER.readValue(jsonList, new TypeReference<AtomList>() {
                });
                final List<AnnotationBodyAtom> m = atomList.getAtomList();
                m.forEach(map -> {
                    log.info("indexing {}", map.getThumbnail());
                    ElasticCreate c = indexer.createDocument(indexName, getDocumentId());
                    sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                    sb.append(System.lineSeparator());
                    sb.append(JsonSerializer.serializeRaw(map.getMetadata()).orElse(""));
                    sb.append(System.lineSeparator());
                });
                log.debug(sb.toString());
                final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
            }
        } catch (IOException | LdpClientException e) {
            log.error(e.getMessage());
        }
    }

    public void putModernContentAtoms(IRI iri, String indexName) {
        final Indexer indexer = new Indexer();
        final String bulkUri = elasticSearchHost + bulkContext;

        final StringBuffer sb = new StringBuffer();
        try {
            final HttpResponse<?> res = client.getResponse(iri);
            if (res.statusCode() == 200) {
                final String jsonList = res.body().toString();
                final AtomList atomList = MAPPER.readValue(jsonList, new TypeReference<AtomList>() {
                });
                final List<AnnotationBodyAtom> m = atomList.getAtomList();
                final List<AnnotationBodyAtom> modernList = m.stream().collect(Collectors.filtering(
                        x -> (Boolean) x.getMetadata().entrySet().stream().anyMatch(
                                y -> y.getKey().contains("Date") && Integer.parseInt((String) y.getValue()) > 1900),
                        Collectors.toList()));
                if (!modernList.isEmpty()) {
                    modernList.forEach(map -> {
                        ElasticCreate c = indexer.createDocument(indexName, getDocumentId());
                        sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                        sb.append(System.lineSeparator());
                        log.info("Scanning Image {}", map.getThumbnail());
                        IRI scannerApi = rdf.createIRI(
                                scannerAPIHost + "?type=scan&lang=deu&image=" + map.getThumbnail());
                        List<ContentList.Content> cList = getContentList(scannerApi).getContentList();
                        map.setContentList(cList);
                        sb.append(JsonSerializer.serializeRaw(map).orElse(""));
                        sb.append(System.lineSeparator());
                    });
                    log.debug(sb.toString());
                    final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                    client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
                }
            }
        } catch (IOException | LdpClientException e) {
            log.error(e.getMessage());
        }
    }

    public ContentList getContentList(IRI iri) {
        try {
            final HttpResponse<?> res = client.getResponse(iri);
            if (res.statusCode() == 200) {
                final String jsonList = res.body().toString();
                return MAPPER.readValue(jsonList, new TypeReference<ContentList>() {
                });
            }
        } catch (IOException | LdpClientException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public MetadataMap buildMetadataMap(final String json) {
        final MetadataMap metadataMap;
        try {
            metadataMap = MAPPER.readValue(
                    json, new TypeReference<MetadataMap>() {
                    });
            return metadataMap;
        } catch (IOException e) {
            log.info("unmappable metadata");
        }
        return null;
    }
}
