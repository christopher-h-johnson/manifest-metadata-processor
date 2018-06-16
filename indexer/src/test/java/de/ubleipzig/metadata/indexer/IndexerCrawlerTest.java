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
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.AnnotationBodyAtom;
import de.ubleipzig.metadata.templates.AtomList;
import de.ubleipzig.metadata.templates.CollectionMapListIdentifier;
import de.ubleipzig.metadata.templates.ElasticCreate;
import de.ubleipzig.metadata.templates.MapList;
import de.ubleipzig.metadata.templates.MapListIdentifier;
import de.ubleipzig.metadata.templates.MetadataMap;
import de.ubleipzig.metadata.templates.MetadataMapIdentifier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jdk.incubator.http.HttpResponse;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

public class IndexerCrawlerTest {

    private static Logger logger = LoggerFactory.getLogger(IndexerCrawlerTest.class);
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private final String contentTypeJson = "application/json";
    private final String elasticSearchHost = "http://localhost:9100/";
    private final String lineSeparator = "line.separator";
    private final String docTypeIndex = "_doc";
    private final String bulkContext = "_bulk";

    private static String getDocumentId() {
        return UUID.randomUUID().toString();
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<IRI> buildIRIList() {
        final String extractorService = "http://localhost:9098/extractor?type=extract&manifest=http://iiif.ub" + "" +
                ".uni-leipzig.de/";
        final int loops = 10300;
        final List<IRI> list = new ArrayList<>();
        for (int i = 9698; i < loops; i++) {
            final String pid = String.format("%010d", i);
            final IRI identifier = rdf.createIRI(extractorService + pid + "/manifest.json");
            list.add(identifier);
        }
        return list;
    }

    @Test
    public void testGetJsonAPI() {
        final List<IRI> list = buildIRIList();
        final List<MetadataMap> mapList = new ArrayList<>();
        list.forEach(i -> {
            try {
                final HttpResponse res = client.getResponse(i);
                if (res.statusCode() == 200) {
                    final String json = res.body().toString();
                    final MetadataMap metadataMap = MAPPER.readValue(json, new TypeReference<MetadataMap>() {
                    });
                    if (metadataMap.getMetadataMap().size() > 0) {
                        mapList.add(metadataMap);
                        logger.info("adding {} to indexable metadata", i.getIRIString());
                    }
                }
            } catch (LdpClientException | IOException e) {
                e.printStackTrace();
            }
        });
        final MapList l = new MapList();
        l.setMapList(mapList);
        final String out = JsonSerializer.serialize(l).orElse("");
        JsonSerializer.writeToFile(out, new File("/tmp/ubl-metadata.json"));
    }


    @Disabled
    @Test
    void putJsonElastic() {
        final String baseUrl = "http://workspaces.ub.uni-leipzig.de:9100";
        final String indexName = "/m";
        final String indexType = "/iiif";
        final Indexer indexer = new Indexer();
        try {
            final String index = "{}";
            final InputStream is = new ByteArrayInputStream(index.getBytes());
            client.put(rdf.createIRI(baseUrl + indexName), is, contentTypeJson);
            final InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/ubl-metadata.json");
            final MapList mapList = MAPPER.readValue(jsonList, new TypeReference<MapList>() {
            });
            final List<MetadataMap> m = mapList.getMapList();
            m.forEach(map -> {
                String json = JsonSerializer.serialize(map).orElse("");
                indexer.indexJson(baseUrl, getDocumentId(), indexName, indexType, json);
            });
            client.put(rdf.createIRI(baseUrl + indexName), is, contentTypeJson);
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonElasticBulk() throws LdpClientException {
        final Indexer indexer = new Indexer();
        final String indexName = "ec";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName,
                IndexerCrawlerTest.class.getResourceAsStream("/vp5-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            final InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/vp-metadata.json");
            final MapListIdentifier mapList = MAPPER.readValue(jsonList, new TypeReference<MapListIdentifier>() {
            });
            final List<MetadataMapIdentifier> m = mapList.getMapListCollection();
            m.forEach(map -> {
                ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                sb.append(System.getProperty(lineSeparator));
                sb.append(JsonSerializer.serializeRaw(map).orElse(""));
                sb.append(System.getProperty(lineSeparator));
            });
            System.out.println(sb.toString());
            final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonCollectionElasticBulk() throws LdpClientException {
        final Indexer indexer = new Indexer();
        final String indexName = "ec3";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName,
                IndexerCrawlerTest.class.getResourceAsStream("/ecodices-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            final InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/ecodices-metadata.json");
            final CollectionMapListIdentifier mapList = MAPPER.readValue(
                    jsonList, new TypeReference<CollectionMapListIdentifier>() {
                    });
            final List<MapListIdentifier> m = mapList.getRootCollection();
            m.forEach(map -> {
                map.getMapListCollection().forEach(ml -> {

                    ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                    sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                    sb.append(JsonSerializer.serializeRaw(ml.getMetadataMap()).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                });
            });
            System.out.println(sb.toString());
            final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonCollectionElasticBulk3() throws LdpClientException {
        final Indexer indexer = new Indexer();
        final String indexName = "m";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName,
                IndexerCrawlerTest.class.getResourceAsStream("/ubl-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/ubl-metadata.json");
            final MapList mapList = MAPPER.readValue(jsonList, new TypeReference<MapList>() {
            });
            final List<MetadataMap> m = mapList.getMapList();
            m.forEach(map -> {
                ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                sb.append(System.getProperty(lineSeparator));
                sb.append(JsonSerializer.serializeRaw(map.getMetadataMap()).orElse(""));
                sb.append(System.getProperty(lineSeparator));
            });
            System.out.println(sb.toString());
            final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonAtomsElasticBulk4() throws LdpClientException {
        final Indexer indexer = new Indexer();
        final String indexName = "t3";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName,
                IndexerCrawlerTest.class.getResourceAsStream("/ubl-atom-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            final IRI apiURI = rdf.createIRI(
                    "http://localhost:9098/extractor?type=disassemble&manifest=http://iiif.ub.uni-leipzig" +
                            ".de/0000009857/manifest.json");
            final HttpResponse res = client.getResponse(apiURI);
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
                System.out.println(sb.toString());
                final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
