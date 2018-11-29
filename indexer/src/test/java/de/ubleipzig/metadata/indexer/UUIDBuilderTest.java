package de.ubleipzig.metadata.indexer;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.collections.MDZIdentifiers;
import de.ubleipzig.metadata.templates.collections.ManifestItem;
import de.ubleipzig.metadata.templates.collections.ManifestUUIDMap;
import de.ubleipzig.metadata.templates.collections.PagedCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class UUIDBuilderTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void buildManifestUUIDs() throws IOException {
        final InputStream is = UUIDBuilderTest.class.getResourceAsStream("/data/harvardArt-manifests.json");
        final PagedCollection pc = MAPPER.readValue(is, new TypeReference<PagedCollection>() {
        });
        final Map<String, Map<String, String>> manifestMap = new HashMap<>();
        final List<ManifestItem> manifests = pc.getManifests();
        manifests.forEach(m -> {
            final Optional<String> manifest = ofNullable(m.getId());
            if (manifest.isPresent()) {
                Map<String, String> manifestKV = new HashMap<>();
                final UUID uuid = UUIDv5.nameUUIDFromNamespaceAndString(UUIDv5.NAMESPACE_URL, manifest.get());
                manifestKV.put("manifest", manifest.get());
                manifestMap.put(uuid.toString(), manifestKV);
            }
        });
        final ManifestUUIDMap map = new ManifestUUIDMap();
        map.setManifestMap(manifestMap);
        String json = JsonSerializer.serialize(map).orElse("");
        JsonSerializer.writeToFile(json, new File("/tmp/hvd-uuidMap.json"));
    }

    @Test
    void buildManifestUUIDsForIdentifierList() {
        try {
            URL dir = UUIDBuilderTest.class.getClassLoader().getResource("data/wales/ids");
            final Map<String, Map<String, String>> manifestMap = new HashMap<>();
            File folder = new File(dir.toURI());
            File[] listOfFiles = folder.listFiles();
            List<File> fileList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(listOfFiles)));
            fileList.forEach(f -> {
                final InputStream is;
                try {
                    is = new FileInputStream(f);
                    MDZIdentifiers list = MAPPER.readValue(is, new TypeReference<MDZIdentifiers>() {
                    });
                    final List<String> mdzIds = list.getIdentifiers();
                    mdzIds.forEach(id -> {
                        Map<String, String> manifestKV = new HashMap<>();
                        final UUID uuid = UUIDv5.nameUUIDFromNamespaceAndString(UUIDv5.NAMESPACE_URL, id);
                        manifestKV.put("manifest", id);
                        manifestMap.put(uuid.toString(), manifestKV);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            final ManifestUUIDMap map = new ManifestUUIDMap();
            map.setManifestMap(manifestMap);
            String json = JsonSerializer.serialize(map).orElse("");
            JsonSerializer.writeToFile(json, new File("/tmp/wales-ids.json"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
