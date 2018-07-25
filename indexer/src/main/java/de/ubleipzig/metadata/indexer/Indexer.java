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

import static de.ubleipzig.metadata.indexer.Constants.bulkContext;
import static de.ubleipzig.metadata.indexer.Constants.contentTypeJson;
import static de.ubleipzig.metadata.indexer.Constants.docTypeIndex;
import static de.ubleipzig.metadata.indexer.Constants.elasticSearchHost;
import static de.ubleipzig.metadata.indexer.Constants.lineSeparator;
import static de.ubleipzig.metadata.indexer.Constants.scannerAPIHost;
import static de.ubleipzig.metadata.processor.JsonSerializer.MAPPER;
import static de.ubleipzig.metadata.processor.QueryUtils.readFile;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jdk.incubator.http.HttpResponse;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;


public class Indexer {
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

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

    public ElasticCreate createDocument(String indexName, String type, String id) {
        final ElasticDocumentObject doc = new ElasticDocumentObject();
        doc.setIndex(indexName);
        doc.setType(type);
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
            e.printStackTrace();
        }
    }

    public void createIndexMapping(String baseUrl, InputStream mapping) {
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode jsonNode = MAPPER.readValue(readFile(mapping), JsonNode.class);
            sb.append(jsonNode.toString());
            System.out.println(sb.toString());
            InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.put(rdf.createIRI(baseUrl), is, "application/json");
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

    public MetadataMap getMetadataMap(IRI iri) {
        try {
            final String extractorService = "http://localhost:9098/extractor?type=extract&m=";
            final String extractorRequest = extractorService + iri.getIRIString();
            final IRI req = rdf.createIRI(extractorRequest);
            final HttpResponse res = client.getResponse(req);
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
            final HttpResponse res = client.getResponse(iri);
            if (res.statusCode() == 200) {
                final String jsonList = res.body().toString();
                final AtomList atomList = MAPPER.readValue(jsonList, new TypeReference<AtomList>() {
                });
                final List<AnnotationBodyAtom> m = atomList.getAtomList();
                m.forEach(map -> {
                    ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                    sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                    sb.append(JsonSerializer.serializeRaw(map).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                });
                LOGGER.debug(sb.toString());
                final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
            }
        } catch (IOException | LdpClientException e) {
            e.printStackTrace();
        }
    }

    public void putModernContentAtoms(IRI iri, String indexName) {
        final Indexer indexer = new Indexer();
        final String bulkUri = elasticSearchHost + bulkContext;

        final StringBuffer sb = new StringBuffer();
        try {
            final HttpResponse res = client.getResponse(iri);
            if (res.statusCode() == 200) {
                final String jsonList = res.body().toString();
                final AtomList atomList = MAPPER.readValue(jsonList, new TypeReference<AtomList>() {
                });
                final List<AnnotationBodyAtom> m = atomList.getAtomList();
                final List<AnnotationBodyAtom> modernList = m.stream().collect(Collectors.filtering(
                        x -> (Boolean) x.getMetadata().entrySet().stream().anyMatch(
                                y -> y.getKey().contains("Date") && Integer.valueOf(y.getValue()) > 1900),
                        Collectors.toList()));
                if (!modernList.isEmpty()) {
                    modernList.forEach(map -> {
                        ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                        sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                        sb.append(System.getProperty(lineSeparator));
                        LOGGER.info("Scanning Image {}", map.getIiifService());
                        IRI scannerApi = rdf.createIRI(
                                scannerAPIHost + "?type=scan&lang=deu&image=" + map.getIiifService());
                        List<ContentList.Content> cList = getContentList(scannerApi).getContentList();
                        map.setContentList(cList);
                        sb.append(JsonSerializer.serializeRaw(map).orElse(""));
                        sb.append(System.getProperty(lineSeparator));
                    });
                    LOGGER.debug(sb.toString());
                    final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                    client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
                }
            }
        } catch (IOException | LdpClientException e) {
            e.printStackTrace();
        }
    }

    public ContentList getContentList(IRI iri) {
        try {
            final HttpResponse res = client.getResponse(iri);
            if (res.statusCode() == 200) {
                final String jsonList = res.body().toString();
                return MAPPER.readValue(jsonList, new TypeReference<ContentList>() {
                });
            }
        } catch (IOException | LdpClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MetadataMap> buildMetadataMap(final String json, final List<MetadataMap> mapList ) {
        final MetadataMap metadataMap;
        try {
            metadataMap = MAPPER.readValue(
                    json, new TypeReference<MetadataMap>() {
                    });
            if (metadataMap.getMetadataMap().size() > 0) {
                mapList.add(metadataMap);
            }
            return mapList;
        } catch (IOException e) {
            throw new RuntimeException("Could not map metadata JSON", e.getCause());
        }
    }
}
