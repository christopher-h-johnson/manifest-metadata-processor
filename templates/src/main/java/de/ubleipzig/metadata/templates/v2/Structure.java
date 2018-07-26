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
    private String label;

    @JsonProperty
    private String within;

    @JsonProperty("ranges")
    private List<String> ranges;

    @JsonProperty("canvases")
    private List<String> canvases;

    @JsonProperty("viewingHint")
    private String viewingHint;

    @JsonProperty("metadata")
    private List<Metadata> metadata;

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
     *
     * @return List
     */
    public List<String> getCanvases() {
        return canvases;
    }

    /**
     * @param canvases List
     */
    public void setCanvases(final List<String> canvases) {
        this.canvases = canvases;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getStructureLabel() {
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
     * @param viewingHint String
     */
    public void setViewingHint(final String viewingHint) {
        this.viewingHint = viewingHint;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getWithin() {
        return within;
    }

    /**
     * @param within String
     */
    public void setWithin(final String within) {
        this.within = within;
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
     * @param ranges List
     */
    public void setRanges(final List<String> ranges) {
        this.ranges = ranges;
    }

    /**
     * @return List
     */
    @JsonIgnore
    public List<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata List
     */
    public void setMetadata(final List<Metadata> metadata) {
        this.metadata = metadata;
    }
}
