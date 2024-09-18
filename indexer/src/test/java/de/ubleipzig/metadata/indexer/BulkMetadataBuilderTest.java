package de.ubleipzig.metadata.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.MapList;
import de.ubleipzig.metadata.templates.MetadataMap;
import de.ubleipzig.metadata.templates.collections.MDZIdentifiers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.rdf.api.IRI;
import org.apache.jena.commonsrdf.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Disabled
public class BulkMetadataBuilderTest {
    private final LdpClient client = new LdpClientImpl();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JenaRDF rdf = new JenaRDF();
    private final String extractorBase = "http://localhost:9098/extractor?type=extract&m=";

    @Test
    public void testGetJsonAPI() {

        final InputStream jsonList = IndexerTest.class.getResourceAsStream("/data/nga/ids/NGAIdentifiers-130000.json");

        try {
            MDZIdentifiers list = MAPPER.readValue(jsonList, new TypeReference<>() {
            });
            final List<String> mdzIds = list.getIdentifiers().stream()
                    .distinct()
                    .collect(Collectors.toList());
            log.info("getting metadata for {} ids", mdzIds.size());
            List<List<String>> subSets = Lists.partition(mdzIds, 5000);
            AtomicInteger it = new AtomicInteger();
            subSets.forEach(ss -> {
                final List<MetadataMap> mapList = new ArrayList<>();
                int i = it.getAndIncrement();
                ss.forEach(id -> {

                    try {
                        final IRI iri = rdf.createIRI(extractorBase + id);
                        final HttpResponse<?> res = client.getResponse(iri);
                        final String body = res.body().toString();
                        if (res.statusCode() == 200 && !body.isEmpty()) {
                            final String json = res.body().toString();
                            final MetadataMap metadataMap = MAPPER.readValue(json, new TypeReference<>() {
                            });
                            if (!metadataMap.getMetadataMap().isEmpty()) {
                                mapList.add(metadataMap);
                                log.info("adding {} to indexable metadata", id);
                            }
                        }
                    } catch (LdpClientException | IOException e) {
                        log.error(e.getMessage());
                    }
                });
                final MapList l = new MapList();
                l.setMapList(mapList);
                final String out = JsonSerializer.serialize(l).orElse("");
                JsonSerializer.writeToFile(out, new File("/tmp/nga-metadata-13-" + i + ".json"));
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
