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

package de.ubleipzig.metadata.templates.collections;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.ubleipzig.metadata.templates.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ManifestItem {

    @Setter
    @Getter
    @JsonProperty("@id")
    private String id;

    @Setter
    @Getter
    @JsonProperty("@type")
    private String type;

    @Setter
    @Getter
    @JsonProperty
    private String label;

    @Getter
    @JsonProperty
    private String thumbnail;

    @Getter
    @JsonProperty
    private List<Related> related;

    @JsonProperty
    private String description;

    @Getter
    @JsonProperty
    private List<Metadata> metadata;

    @JsonProperty
    private SeeAlso seeAlso;

    private static class SeeAlso {
        @Getter
        @JsonProperty("@id")
        private String id;

        @JsonProperty
        private String format;

    }

    private static class Related {
        @JsonProperty("@id")
        private String id;

        @JsonProperty
        private String format;

        @JsonProperty
        private String label;

        public String getLabel() {
            return label;
        }

        public String getId() {
            return id;
        }
    }

    @JsonProperty
    private String location;

}
