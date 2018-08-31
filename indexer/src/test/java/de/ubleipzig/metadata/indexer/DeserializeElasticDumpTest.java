package de.ubleipzig.metadata.indexer;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.collections.ManifestUUIDMap;
import de.ubleipzig.metadata.templates.indexer.ElasticDocumentObjectDeserialize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class DeserializeElasticDumpTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void deserializeDoc() {
        String filename = "/tmp/ox1.json";
        final Map<String, Map<String, String>> manifestMap = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            stream.forEach(l -> {
                final ElasticDocumentObjectDeserialize doc;
                try {
                    doc = MAPPER.readValue(l, new TypeReference<ElasticDocumentObjectDeserialize>() {
                    });
                    final Optional<String> manifest = ofNullable(doc.getSource().getManifest());
                    if (manifest.isPresent()) {
                        String man = manifest.get();
                        if (!man.contains("https")) {
                            man = man.replace("http", "https");
                        }
                        Map<String, String> manifestKV = new HashMap<>();
                        final UUID uuid = UUIDv5.nameUUIDFromNamespaceAndString(UUIDv5.NAMESPACE_URL, man);
                        manifestKV.put("manifest", man);
                        manifestMap.put(uuid.toString(), manifestKV);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            final ManifestUUIDMap map = new ManifestUUIDMap();
            map.setManifestMap(manifestMap);
            String json = JsonSerializer.serialize(map).orElse("");
            JsonSerializer.writeToFile(json, new File("/tmp/ox1-uuidMap.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
