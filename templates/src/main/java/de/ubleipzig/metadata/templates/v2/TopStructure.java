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
package de.ubleipzig.metadata.templates.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.ubleipzig.iiif.vocabulary.SCEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TemplateTopStructure.
 *
 * @author christopher-johnson
 */
@JsonPropertyOrder({"@id", "@type", "label", "viewingHint", "ranges"})
public class TopStructure extends Structure {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SCEnum.Range.compactedIRI();

    @JsonProperty("label")
    private String label;

    @JsonProperty("viewingHint")
    private String viewingHint = "top";

    @JsonIgnore
    private List<String> canvases;

    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return List
      * @param ranges List
     */
    @Setter
    @Getter
    @JsonProperty("ranges")
    private List<String> ranges;

    /**
     *
     */
    public TopStructure() {
    }

    /**
     * @param id String
     */
    public void setStructureId(final String id) {
        this.id = id;
    }

    /**
     * @param structureLabel String
     */
    public void setStructureLabel(final String structureLabel) {
        this.label = structureLabel;
    }

}
