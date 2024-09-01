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
package de.ubleipzig.metadata.templates;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public class ISO639 {
    @JsonProperty
    private List<Language> languages;

    public List<Language> getLanguages() {
        return languages;
    }

    public static class Language {
        @Getter
        @JsonProperty
        private String iso639_2;

        @JsonProperty
        private String iso639_1;

        @Getter
        @JsonProperty
        private String englishName;

        @Getter
        @JsonProperty
        private String frenchName;

        @Getter
        @JsonProperty
        private String germanName;

    }
}
