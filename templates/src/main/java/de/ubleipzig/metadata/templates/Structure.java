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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * TemplateStructure.
 *
 * @author christopher-johnson
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"@id", "@type", "label", "ranges", "canvases"})
public class Structure {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("label")
    private String label;

    @JsonProperty("ranges")
    private List<String> ranges;

    @JsonProperty("canvases")
    private List<String> canvases;

    @JsonProperty("viewingHint")
    private String viewingHint;

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
        return this.label;
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
    public String getStructureId() {
        return this.id;
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
        return this.ranges;
    }

    /**
     * @param ranges List
     */
    public void setRanges(final List<String> ranges) {
        this.ranges = ranges;
    }
}
