package de.ubleipzig.metadata.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CollectionList {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty
    private String label;

    @JsonProperty
    private List<Manifest> collections;

    @JsonProperty
    private String description;

    @JsonProperty
    private String logo;

    @JsonProperty
    private String attribution;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public List<Manifest> getCollections() {
        return collections;
    }

    public void setCollections(List<Manifest> collections) {
        this.collections = collections;
    }
}
