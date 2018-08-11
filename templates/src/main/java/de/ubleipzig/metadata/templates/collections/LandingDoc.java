package de.ubleipzig.metadata.templates.collections;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LandingDoc {
    @JsonProperty
    private String tag1;

    @JsonProperty
    private String tag2;

    @JsonProperty
    private String tag3;

    @JsonProperty
    private String tag4;

    @JsonProperty
    private String imageServiceIRI;

    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public void setTag3(String tag3) {
        this.tag3 = tag3;
    }

    public void setTag4(String tag4) {
        this.tag4 = tag4;
    }

    public void setImageServiceIRI(String imageServiceIRI) {
        this.imageServiceIRI = imageServiceIRI;
    }
}
