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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.ubleipzig.iiif.vocabulary.SCEnum;
import de.ubleipzig.metadata.templates.Metadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * TemplateStructure.
 *
 * @author christopher-johnson
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"@id", "@type", "label", "viewingHint", "ranges", "canvases"})
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Structure {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SCEnum.Range.compactedIRI();

    @JsonProperty("label")
    private Object label;

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

    @JsonProperty("attribution")
    private String attribution;

    @JsonProperty("description")
    private Object description;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("license")
    private String license;

}
