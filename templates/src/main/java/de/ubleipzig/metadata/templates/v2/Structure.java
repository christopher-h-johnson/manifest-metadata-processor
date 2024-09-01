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

/**
 * TemplateStructure.
 *
 * @author christopher-johnson
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"@id", "@type", "label", "viewingHint", "ranges", "canvases"})
public class Structure {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SCEnum.Range.compactedIRI();

    @JsonProperty("label")
    private Object label;

    /**
     * -- SETTER --
     *
     * @param within String
     */
    @Setter
    @JsonProperty
    private String within;

    /**
     * -- SETTER --
     *
     * @param ranges List
     */
    @Setter
    @JsonProperty("ranges")
    private List<String> ranges;

    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return List
      * @param canvases List
     */
    @Setter
    @Getter
    @JsonProperty("canvases")
    private List<String> canvases;

    /**
     * -- SETTER --
     *
     * @param viewingHint String
     */
    @Setter
    @JsonProperty("viewingHint")
    private String viewingHint;

    /**
     * -- SETTER --
     *
     * @param metadata List
     */
    @Setter
    @JsonProperty("metadata")
    private List<Metadata> metadata;

    @JsonProperty("attribution")
    private String attribution;

    @JsonProperty("description")
    private String description;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("license")
    private String license;

    /**
     *
     */
    public Structure() {
    }

    /**
     * @param structureType String
     */
    public void setStructureType(final String structureType) {
        this.type = structureType;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public Object getStructureLabel() {
        return label;
    }

    /**
     * @param structureLabel String
     */
    public void setStructureLabel(final String structureLabel) {
        this.label = structureLabel;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getViewingHint() {
        return viewingHint;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getWithin() {
        return within;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getStructureId() {
        return id;
    }

    /**
     * @param id String
     */
    public void setStructureId(final String id) {
        this.id = id;
    }

    /**
     * @return List
     */
    @JsonIgnore
    public List<String> getRanges() {
        return ranges;
    }

    /**
     * @return List
     */
    @JsonIgnore
    public List<Metadata> getMetadata() {
        return metadata;
    }

}
