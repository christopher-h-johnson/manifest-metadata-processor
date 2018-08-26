package de.ubleipzig.metadata.templates.indexer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"Artist",
        "Birthplace",
        "Culture & Date",
        "Collection",
        "Dimensions",
        "Gallery Label (created)",
        "Inscription",
        "Markings",
        "Medium",
        "Object Number",
        "Object Type",
        "Place Created",
        "Place Depicted",
        "Place Found",
        "Rights Statement",
        "Signed",
        "related",
        "thumbnail",
        "title",
        "within"})
public class Source {
    @JsonProperty
    private String id;

    public String getId() {
        return id;
    }
}
