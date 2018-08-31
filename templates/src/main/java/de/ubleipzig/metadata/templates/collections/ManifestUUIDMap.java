package de.ubleipzig.metadata.templates.collections;

import java.util.Map;

public class ManifestUUIDMap {
    private Map<String,Map<String, String>> manifestMap;

    public Map<String,Map<String, String>> getManifestMap() {
        return manifestMap;
    }

    public void setManifestMap(Map<String,Map<String, String>> manifestMap) {
        this.manifestMap = manifestMap;
    }
}
