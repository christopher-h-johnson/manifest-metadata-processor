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

import java.util.List;

public class Manifest {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty
    private String label;

    @JsonProperty
    private String license;

    @JsonProperty
    private String within;

    @JsonProperty
    private Object description;

    @JsonProperty
    private String attribution;

    @JsonProperty
    private List<Metadata> metadata;

    @JsonProperty
    private String logo;

    @JsonProperty
    private Object related;

    @JsonProperty
    private List<Object> service;

    @JsonProperty
    private List<Object> seeAlso;

    @JsonProperty
    private List<Structure> structures;

    @JsonProperty
    private String viewingHint;

    @JsonProperty
    private Object thumbnail;

    @JsonProperty
    private List<Sequences> sequences;

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Sequences> getSequences() {
        return sequences;
    }

    public void setSequences(List<Sequences> sequences) {
        this.sequences = sequences;
    }

    public List<Structure> getStructures() {
        return structures;
    }

}
