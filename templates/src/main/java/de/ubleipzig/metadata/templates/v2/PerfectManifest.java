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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * Manifest.
 *
 * @author christopher-johnson
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"@context", "@id", "@type", "label", "license", "attribution", "logo", "related", "metadata",
        "sequences", "service"})
public class PerfectManifest {

    /**
     * -- SETTER --
     *
     * @param context String
     */
    @Setter
    @JsonIgnoreProperties({"rendering", "logo"})

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

    /**
     * -- SETTER --
     *
     * @param type String
     */
    @Setter
    @JsonProperty("@type")
    private String type = SCEnum.Manifest.compactedIRI();

    /**
     * -- SETTER --
     *
     *
     * -- GETTER --
     *
     @param label String
      * @return String
     */
    @Getter
    @Setter
    @JsonProperty("label")
    private String label;

    /**
     * -- SETTER --
     *
     * @param license String
     */
    @Getter
    @Setter
    @JsonProperty("license")
    private String license;

    @JsonProperty
    private String viewingDirection;

    @JsonProperty
    private String within;

    /**
     * -- GETTER --
     *
     * @return List
     */
    @Getter
    @JsonProperty
    private Object description;

    /**
     * -- SETTER --
     *
     * @param attribution String
     */
    @Getter
    @Setter
    @JsonProperty
    private String attribution;

    @Getter
    @JsonProperty
    private Object thumbnail;

    /**
     * -- SETTER --
     *
     * @param logo String
     */
    @Setter
    @JsonProperty("logo")
    private Object logo;

    /**
     * -- SETTER --
     *
     *
     * -- GETTER --
     *
     @param related List
      * @return List
     */
    @Getter
    @Setter
    @JsonProperty("related")
    private Object related;

    /**
     * -- SETTER --
     *
     *
     * -- GETTER --
     *
     @param sequences List
      * @return List
     */
    @Getter
    @Setter
    @JsonProperty("sequences")
    private List<Sequence> sequences;

    /**
     * -- SETTER --
     *
     * @param service String
     */
    @Setter
    @JsonProperty("service")
    private  Object service;

    /**
     * -- SETTER --
     *
     *
     * -- GETTER --
     *
     @param seeAlso List
      * @return List
     */
    @Getter
    @Setter
    @JsonProperty
    private Object seeAlso;

    /**
     * -- SETTER --
     *
     *
     * -- GETTER --
     *
     @param metadata List
      * @return List
     */
    @Getter
    @Setter
    @JsonProperty("metadata")
    private List<Metadata> metadata;

    /**
     * -- SETTER --
     *
     * @param structures List
     */
    @Setter
    @JsonProperty("structures")
    private List<Structure> structures;

    @JsonProperty
    private String viewingHint;

    @JsonProperty
    private Object rendering;

    @JsonProperty
    private String navDate;

    /**
     * Manifest.
     */
    public PerfectManifest() {
    }


}


