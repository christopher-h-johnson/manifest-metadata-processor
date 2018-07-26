package de.ubleipzig.metadata.templates;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ISO639 {
    @JsonProperty
    private List<Language> languages;

    public List<Language> getLanguages() {
        return languages;
    }

    public static class Language {
        @JsonProperty
        private String iso639_2;

        @JsonProperty
        private String iso639_1;

        @JsonProperty
        private String englishName;

        @JsonProperty
        private String frenchName;

        @JsonProperty
        private String germanName;

        public String getEnglishName() {
            return englishName;
        }

        public String getFrenchName() {
            return frenchName;
        }

        public String getGermanName() {
            return germanName;

        }

        public String getIso639_2() {
            return iso639_2;
        }
    }
}