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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.ubleipzig.iiif.vocabulary.SCEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Sequence.
 *
 * @author christopher-johnson
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"@id", "@type", "viewingHint", "canvases"})
public class Sequence {

    @JsonProperty("@context")
    private String context;

    /**
     * -- SETTER --
     *
     * @param id String
     */
    @Getter
    @Setter
    @JsonProperty("@id")
    private String id;

    /**
     * -- SETTER --
     *
     * @param type String
     */
    @Setter
    @JsonProperty("@type")
    private String type = SCEnum.Sequence.compactedIRI();

    @JsonProperty
    private Object label;

    @JsonProperty
    private List<Object> rendering;

    /**
     * -- SETTER --
     *
     * @param viewingHint String
     */
    @Setter
    @JsonProperty
    private String viewingHint;

    @JsonProperty
    private String startCanvas;

    @JsonProperty
    private String viewingDirection;

    /**
     * -- SETTER --
     *
     *
     * -- GETTER --
     *
     @param canvases List
      * @return List
     */
    @Getter
    @Setter
    @JsonProperty
    private List<Canvas> canvases;

    public Sequence() {
    }


}
