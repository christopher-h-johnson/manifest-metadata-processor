package de.ubleipzig.metadata.templates.indexer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({
        "accessRights",
        "alternatives",
        "attribution",
        "catalogueId",
        "collections",
        "contributors",
        "coverages",
        "creators",
        "dates",
        "description",
        "descriptions",
        "displayLanguages",
        "formats",
        "identifiers",
        "incipits",
        "languages",
        "locations",
        "publisher",
        "related",
        "shelfmark",
        "sources",
        "subjects",
        "thumbnail",
        "title",
        "titles",
        "types",
        "seeAlso"})
public class Source {
    @JsonProperty
    private String manifest;

    public String getManifest() {
        return manifest;
    }
}
