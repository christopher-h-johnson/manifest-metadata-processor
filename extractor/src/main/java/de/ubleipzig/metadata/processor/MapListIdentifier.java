package de.ubleipzig.metadata.processor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MapListIdentifier {

    @JsonProperty
    private List<MetadataMapIdentifier> mapList;

    public void setMapList(List<MetadataMapIdentifier> mapList) {
        this.mapList = mapList;
    }

    @JsonIgnore
    public List<MetadataMapIdentifier> getMapList() {
        return mapList;
    }
}
