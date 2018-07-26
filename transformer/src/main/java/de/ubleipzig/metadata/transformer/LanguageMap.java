package de.ubleipzig.metadata.transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.templates.ISO639;

import java.io.IOException;

public class LanguageMap {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private ISO639 iso639;

    public LanguageMap() {
        this.iso639 = mapLanguages();
    }

    public ISO639 getISO639() {
        return iso639;
    }

    public ISO639 mapLanguages() {
        try {
            return MAPPER.readValue(
                    MetadataImplVersion2.class.getResourceAsStream("/iso639-2.json"), new TypeReference<ISO639>() {
                    });
        } catch (IOException e) {
            throw new RuntimeException("could not read language map " + e.getMessage());
        }
    }
}
