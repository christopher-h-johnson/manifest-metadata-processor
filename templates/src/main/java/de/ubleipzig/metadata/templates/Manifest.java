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
import de.ubleipzig.metadata.templates.v2.Structure;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Manifest {

    @JsonProperty("@context")
    private String context;

    @Getter
    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @Setter
    @Getter
    @JsonProperty
    private String label;

    @Getter
    @JsonProperty
    private String license;

    @JsonProperty
    private String viewingDirection;

    @JsonProperty
    private String within;

    @JsonProperty
    private Object description;

    @Getter
    @JsonProperty
    private String attribution;

    @Getter
    @JsonProperty
    private List<Metadata> metadata;

    @JsonProperty
    private String logo;

    @Getter
    @JsonProperty
    private Object related;

    @JsonProperty
    private List<Object> service;

    @JsonProperty
    private List<Object> seeAlso;

    @Getter
    @JsonProperty
    private List<Structure> structures;

    @JsonProperty
    private String viewingHint;

    @Getter
    @JsonProperty
    private Object thumbnail;

    @Setter
    @Getter
    @JsonProperty
    private List<Sequences> sequences;

}
