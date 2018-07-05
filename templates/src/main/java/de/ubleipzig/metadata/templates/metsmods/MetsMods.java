package de.ubleipzig.metadata.templates.metsmods;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class MetsMods {
    @JsonProperty
    private List<Map<String, Object>> structures;

    public List<Map<String, Object>> getStructures() {
        return structures;
    }
    @JsonProperty
    private Map<String, Object> metadata;

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
