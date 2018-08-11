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

    @JsonIgnoreProperties({"rendering", "logo"})

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SCEnum.Manifest.compactedIRI();

    @JsonProperty("label")
    private String label;

    @JsonProperty("license")
    private String license;

    @JsonProperty
    private String viewingDirection;

    @JsonProperty
    private String within;

    @JsonProperty
    private Object description;

    @JsonProperty
    private String attribution;

    @JsonProperty
    private Object thumbnail;

    @JsonProperty("logo")
    private Object logo;

    @JsonProperty("related")
    private Object related;

    @JsonProperty("sequences")
    private List<Sequence> sequences;

    @JsonProperty("service")
    private  Object service;

    @JsonProperty
    private Object seeAlso;

    @JsonProperty("metadata")
    private List<Metadata> metadata;

    @JsonProperty("structures")
    private List<Structure> structures;

    @JsonProperty
    private String viewingHint;

    @JsonProperty
    private Object rendering;


    /**
     * Manifest.
     */
    public PerfectManifest() {
    }

    /**
     * @param context String
     */
    public void setContext(final String context) {
        this.context = context;
    }

    /**
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * @param id String
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param type String
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @param label String
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param attribution String
     */
    public void setAttribution(final String attribution) {
        this.attribution = attribution;
    }

    public Object getThumbnail() {
        return thumbnail;
    }

    /**
     * @param license String
     */
    public void setLicense(final String license) {
        this.license = license;
    }

    /**
     * @param logo String
     */
    public void setLogo(final Object logo) {
        this.logo = logo;
    }

    /**
     * @param service String
     */
    public void setService(final Object service) {
        this.service = service;
    }

    /**
     * @param sequences List
     */
    public void setSequences(final List<Sequence> sequences) {
        this.sequences = sequences;
    }

    /**
     * @return List
     */
    public List<Sequence> getSequences() {
        return sequences;
    }

    /**
     * @param structures List
     */
    public void setStructures(final List<Structure> structures) {
        this.structures = structures;
    }

    /**
     * @param metadata List
     */
    public void setMetadata(final List<Metadata> metadata) {
        this.metadata = metadata;
    }

    /**
     * @return List
     */
    public List<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * @param related List
     */
    public void setRelated(final Object related) {
        this.related = related;
    }

    /**
     * @return List
     */
    public Object getRelated() {
        return related;
    }

    /**
     * @param seeAlso List
     */
    public void setSeeAlso(final Object seeAlso) {
        this.seeAlso = seeAlso;
    }

    /**
     * @return List
     */
    public Object getSeeAlso() {
        return seeAlso;
    }
}


