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

import static de.ubleipzig.metadata.processor.QueryUtils.readFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.ElasticCreate;
import de.ubleipzig.metadata.templates.ElasticIndex;
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

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

import jdk.incubator.http.HttpResponse;

public class IndexerCrawlerTest {

    private static Logger logger = LoggerFactory.getLogger(IndexerCrawlerTest.class);
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final String extractorService = "http://localhost:9098/extractor?type=extract&manifest=http://iiif" +
            ".ub.uni-leipzig.de/";
    private static final String elasticBaseUrl = "http://localhost:9100";

    private static String getDocumentId() {
        return UUID.randomUUID().toString();
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<IRI> buildIRIList() {
        final int LOOPS = 10000;
        List<IRI> list = new ArrayList<>();
        for (int i = 0; i < LOOPS; i++) {
            final String pid = String.format("%010d", i);
            final IRI identifier = rdf.createIRI(extractorService + pid + "/manifest.json");
            list.add(identifier);
        }
        return list;
    }

    @Disabled
    @Test
    public void testGetJsonAPI() {
        final String baseUrl = "http://workspaces.ub.uni-leipzig.de:9100";
        final String indexName = "/m";
        final String indexType = "/iiif";
        List<IRI> list = buildIRIList();
        List<MetadataMap> mapList = new ArrayList<>();
        list.forEach(i -> {
            try {
                HttpResponse res = client.getResponse(i);
                if (res.statusCode() == 200) {
                    String json = res.body().toString();
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
        MapList l = new MapList();
        l.setMapList(mapList);
        final String out = JsonSerializer.serialize(l).orElse("");
        JsonSerializer.writeToFile(out, new File("/tmp/ubl-metadata.json"));
        try {
            String index = "{}";
            InputStream is = new ByteArrayInputStream(index.getBytes());
            client.put(rdf.createIRI(elasticBaseUrl + indexName), is, "application/json");
        } catch (LdpClientException e) {
            e.printStackTrace();
        }
        Indexer indexer = new Indexer();
        mapList.forEach(m -> {
            String json = JsonSerializer.serialize(m).orElse("");
            indexer.indexJson(baseUrl, getDocumentId(), indexName, indexType, json);
        });
    }


    @Disabled
    @Test
    void putJsonElastic() {
        String baseUrl = "http://workspaces.ub.uni-leipzig.de:9100";
        String indexName = "/m";
        final String indexType = "/iiif";
        Indexer indexer = new Indexer();
        try {
            String index = "{}";
            InputStream is = new ByteArrayInputStream(index.getBytes());
            client.put(rdf.createIRI(baseUrl + indexName), is, "application/json");
            InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/ubl-metadata.json");
            final MapList mapList = MAPPER.readValue(jsonList, new TypeReference<MapList>() {
            });
            final List<MetadataMap> m = mapList.getMapList();
            m.forEach(map -> {
                String json = JsonSerializer.serialize(map).orElse("");
                indexer.indexJson(baseUrl, getDocumentId(), indexName, indexType, json);
            });
            client.put(rdf.createIRI(baseUrl + indexName), is, "application/json");
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateIndexandMapping() throws IOException, LdpClientException {
        final Indexer indexer = new Indexer();
        final String baseUrl = "http://localhost:9100/_bulk";
        StringBuilder sb = new StringBuilder();
        ElasticIndex i = indexer.createIndex("vp", "iiif", getDocumentId());
        sb.append(JsonSerializer.serializeRaw(i).orElse(""));
        sb.append(System.getProperty("line.separator"));
        JsonNode jsonNode = MAPPER.readValue(
                readFile(IndexerCrawlerTest.class.getResourceAsStream("/vp5-mapping.json")), JsonNode.class);
        sb.append(jsonNode.toString());
        sb.append(System.getProperty("line.separator"));
        System.out.println(sb.toString());
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
        client.post(rdf.createIRI(baseUrl), is, "application/json");
    }

    @Test
    void putJsonElasticBulk() throws IOException, LdpClientException {
        final Indexer indexer = new Indexer();
        final String baseUrl = "http://localhost:9100/_bulk";
        StringBuffer sb = new StringBuffer();
        ElasticIndex i = indexer.createIndex("vp", "iiif", getDocumentId());
        sb.append(JsonSerializer.serializeRaw(i).orElse(""));
        sb.append(System.getProperty("line.separator"));
        JsonNode jsonNode = MAPPER.readValue(
                readFile(IndexerCrawlerTest.class.getResourceAsStream("/vp5-mapping.json")), JsonNode.class);
        sb.append(jsonNode.toString());
        sb.append(System.getProperty("line.separator"));
        try {
            InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/vp-metadata.json");
            final MapListIdentifier mapList = MAPPER.readValue(jsonList, new TypeReference<MapListIdentifier>() {
            });
            final List<MetadataMapIdentifier> m = mapList.getMapList();
            m.forEach(map -> {
                ElasticCreate c = indexer.createDocument("vp", "iiif", getDocumentId());
                sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                sb.append(System.getProperty("line.separator"));
                sb.append(JsonSerializer.serializeRaw(map).orElse(""));
                sb.append(System.getProperty("line.separator"));
                //indexer.indexJson(baseUrl, getDocumentId(), indexName, indexType, json);
            });
            System.out.println(sb.toString());
            InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(baseUrl), is, "application/json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
