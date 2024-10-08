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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Canvases {

    @Getter
    @JsonIgnoreProperties({"label", "thumbnail", "seeAlso", "height", "width", "otherContent"})

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    /**
     * -- SETTER --
     *
     * @param label String
     */
    @Setter
    @JsonProperty
    private String label;

    @JsonProperty
    private Object thumbnail;

    @JsonProperty
    private String height;

    @JsonProperty
    private String width;

    @JsonProperty
    private Object seeAlso;

    @JsonProperty
    private List<Object> otherContent;

    @Setter
    @Getter
    @JsonProperty
    private List<Images> images;

    /**
     * @return String
     */
    @JsonIgnore
    public String getLabel() {
        return this.label;
    }

}
