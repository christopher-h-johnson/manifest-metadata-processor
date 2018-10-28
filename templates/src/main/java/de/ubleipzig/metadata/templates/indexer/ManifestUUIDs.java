package de.ubleipzig.metadata.templates.indexer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ManifestUUIDs {

    @JsonProperty
    private Map<String, Object> manifestMap;

    public Map<String,Object> getManifestMap() {
        return manifestMap;
    }

    public void setManifestMap(final Map<String, Object> manifestMap) {
        this.manifestMap = manifestMap;
    }

}
