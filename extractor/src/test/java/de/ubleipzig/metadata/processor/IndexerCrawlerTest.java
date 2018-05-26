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

package de.ubleipzig.metadata.processor;

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static de.ubleipzig.metadata.processor.JsonSerializer.writeToFile;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

public class IndexerCrawlerTest {

    private static Logger logger = getLogger(IndexerCrawlerTest.class);
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final String baseUrl =
            "http://localhost:9098/extractor?type=extract&manifest=http://iiif.ub.uni-leipzig.de/";
    private static final String elasticBaseUrl = "http://localhost:9100";
    private static final String indexName = "/m";
    private static final String indexType = "/iiif";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<IRI> buildIRIList() {
        final int LOOPS = 10000;
        List<IRI> list = new ArrayList<>();
        for (int i = 0; i < LOOPS; i++) {
            final String pid = String.format("%010d", i);
            final IRI identifier = rdf.createIRI(baseUrl + pid + "/manifest.json");
            list.add(identifier);
        }
        return list;
    }

    private static String getDocumentId() {
        return UUID.randomUUID().toString();
    }

    @Disabled
    @Test
    public void testGetJsonAPI() {
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
        final String out = serialize(l).orElse("");
        writeToFile(out, new File("/tmp/ubl-metadata.json"));
        try {
            String index = "{}";
            InputStream is = new ByteArrayInputStream(index.getBytes());
            client.put(rdf.createIRI(elasticBaseUrl + indexName), is, "application/json");
        } catch (LdpClientException e) {
            e.printStackTrace();
        }
        mapList.forEach(m -> {
            String json = serialize(m).orElse("");
            indexJson(json, indexName, indexType);
        });
    }

    public void indexJson(String json, String indexName, String indexType) {
        final IRI identifier = rdf.createIRI(elasticBaseUrl + indexName + indexType + "/" + getDocumentId());
        InputStream is = new ByteArrayInputStream(json.getBytes());
        try {
            client.put(identifier, is, "application/json");
        } catch (LdpClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonElastic() {
        String indexName = "/m6";
        try {
            String index = "{}";
            InputStream is = new ByteArrayInputStream(index.getBytes());
            client.put(rdf.createIRI(elasticBaseUrl + "/vp"), is, "application/json");
            InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/ubl-metadata.json");
            final MapList mapList = MAPPER.readValue(jsonList, new TypeReference<MapList>() {
            });
            final List<MetadataMap> m = mapList.getMapList();
            m.forEach(map -> {
                String json = serialize(map).orElse("");
                indexJson(json, indexName, indexType);
            });
            client.put(rdf.createIRI(elasticBaseUrl + indexName), is, "application/json");
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonElastic2() {
        try {
            String indexName = "/vp5";
            String index = "{}";
            InputStream is = new ByteArrayInputStream(index.getBytes());
            client.put(rdf.createIRI(elasticBaseUrl + indexName), is, "application/json");
            InputStream jsonList = IndexerCrawlerTest.class.getResourceAsStream("/vp-metadata.json");
            final MapListIdentifier mapList = MAPPER.readValue(jsonList, new TypeReference<MapListIdentifier>() {
            });
            final List<MetadataMapIdentifier> m = mapList.getMapList();
            m.forEach(map -> {
                String json = serialize(map).orElse("");
                indexJson(json, indexName, indexType);
            });
            client.put(rdf.createIRI(elasticBaseUrl + indexName), is, "application/json");
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

}
