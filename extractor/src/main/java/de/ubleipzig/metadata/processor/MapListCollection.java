package de.ubleipzig.metadata.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MapListCollection {

    @JsonProperty
    private List<MetadataMap> mapListCollection;

    @JsonProperty("@id")
    private String id;

    @JsonProperty
    private String label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<MetadataMap> getMapListCollection() {
        return mapListCollection;
    }

    public void setMapListCollection(List<MetadataMap> mapListCollection) {
        this.mapListCollection = mapListCollection;
    }

}
