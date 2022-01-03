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
import de.ubleipzig.metadata.templates.collections.ManifestUUIDMap;
import de.ubleipzig.metadata.templates.indexer.ElasticDocumentObjectDeserialize;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Disabled
public class DeserializeElasticDumpTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void deserializeDoc() {
        String filename = "/tmp/dh1.json";
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
            JsonSerializer.writeToFile(json, new File("/tmp/dh1-uuidMap.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
