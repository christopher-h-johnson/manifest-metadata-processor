package de.ubleipzig.metadata.templates.collections;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MDZIdentifiers {
    @JsonProperty
    List<String> identifiers;

    public List<String> getIdentifiers() {
        return identifiers;
    }
}
