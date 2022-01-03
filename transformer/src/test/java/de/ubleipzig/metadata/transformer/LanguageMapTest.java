/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ubleipzig.metadata.transformer;

import de.ubleipzig.metadata.templates.ISO639;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

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
