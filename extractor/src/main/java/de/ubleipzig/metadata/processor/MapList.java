package de.ubleipzig.metadata.processor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MapList {

    @JsonProperty
    private List<MetadataMap> mapList;

    public void setMapList(List<MetadataMap> mapList) {
        this.mapList = mapList;
    }

    @JsonIgnore
    public List<MetadataMap> getMapList() {
        return mapList;
    }
}
