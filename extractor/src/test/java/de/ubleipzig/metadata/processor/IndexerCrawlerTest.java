package de.ubleipzig.metadata.processor;

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static de.ubleipzig.metadata.processor.JsonSerializer.writeToFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Test;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

public class IndexerCrawlerTest {
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final String baseUrl = "http://localhost:9098/extractor?type=extract&manifest=http://iiif.ub" +
            ".uni-leipzig.de/";
    private static final String elasticBaseUrl = "http://localhost:9200";
    private static final String indexName = "/manifests1";
    private static final String indexType = "/iiif";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<IRI> buildIRIList() {
        final int LOOPS = 10;
        List<IRI> list = new ArrayList<>();
        for (int i = 0; i < LOOPS; i++) {
            final String pid = String.format("%010d", i);
            final IRI identifier = rdf.createIRI(baseUrl + pid + "/manifest.json");
            list.add(identifier);
        }
        return list;
    }

    private static String getDocumentId() {
        return UUID.randomUUID()
                .toString();
    }

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
           String json =  serialize(m).orElse("");
           indexJson(json);
        });
    }

    @Test
    public void indexJson(String json) {
        final IRI identifier = rdf.createIRI(elasticBaseUrl + indexName + indexType + "/" + getDocumentId());
        InputStream is = new ByteArrayInputStream(json.getBytes());
        try {
            client.put(identifier, is, "application/json" );
        } catch (LdpClientException e) {
            e.printStackTrace();
        }
    }
}
