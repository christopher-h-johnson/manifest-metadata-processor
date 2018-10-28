package de.ubleipzig.metadata.templates.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LabelObject {

    @JsonProperty("@language")
    String language;

    @JsonProperty("@value")
    String value;

    public String getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
