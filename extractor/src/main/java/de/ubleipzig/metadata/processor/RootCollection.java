package de.ubleipzig.metadata.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RootCollection {
    @JsonProperty
    List<MapListCollection> rootCollection;

    public List<MapListCollection> getRootCollection() {
        return rootCollection;
    }

    public void setRootCollection(List<MapListCollection> rootCollection) {
        this.rootCollection = rootCollection;
    }
}
