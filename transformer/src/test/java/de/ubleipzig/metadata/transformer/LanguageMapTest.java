package de.ubleipzig.metadata.transformer;

import de.ubleipzig.metadata.templates.ISO639;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class LanguageMapTest {

    @Test
    void testGetLanguageMap() {
       final MetadataImplVersion2 metadataImplVersion2 = new MetadataImplVersion2();
        final LanguageMap languageMap = new LanguageMap();
        final ISO639 iso639 = languageMap.getISO639();
        final List<ISO639.Language> languages = iso639.getLanguages();
        final Optional<ISO639.Language> german = languages.stream().filter(
                y -> y.getIso639_2().equals("ger")).findAny();
        if (german.isPresent()) {
            final String name = german.get().getGermanName();
            System.out.println(name);
        }
    }
}
