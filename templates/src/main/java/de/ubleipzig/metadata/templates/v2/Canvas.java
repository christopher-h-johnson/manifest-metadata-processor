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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.ubleipzig.iiif.vocabulary.SCEnum;
import de.ubleipzig.metadata.templates.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"@id", "@type", "label", "height", "width", "images"})
public class Canvas {

    @JsonProperty("@context")
    private String context;

    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return String
      * @param id String
     */
    @Setter
    @Getter
    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SCEnum.Canvas.compactedIRI();

    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return List
      * @param images List
     */
    @Setter
    @Getter
    @JsonProperty("images")
    private List<PaintingAnnotation> images;

    @JsonProperty("metadata")
    private List<Metadata> metadata;

    /**
     * -- SETTER --
     *
     * @param label String
     */
    @Setter
    @JsonProperty("label")
    private String label;

    /**
     * -- SETTER --
     *
     * @param height int
     */
    @Setter
    @JsonProperty("height")
    private Integer height;

    /**
     * -- SETTER --
     *
     * @param width int
     */
    @Setter
    @JsonProperty("width")
    private Integer width;

    @JsonProperty("thumbnail")
    private Object thumbnail;

    @JsonProperty("related")
    private Object related;

    @JsonProperty("service")
    private Object service;

    @JsonProperty
    private List<Object> otherContent;

    /**
     * @return int
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return int
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getLabel() {
        return this.label;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public List<Metadata> getMetadata() {
        return this.metadata;
    }

}
