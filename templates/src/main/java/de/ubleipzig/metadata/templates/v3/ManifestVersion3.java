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

package de.ubleipzig.metadata.templates.v3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"@context", "id", "type", "label", "summary", "thumbnail", "viewingDirection", "behavior",
        "navDate", "rights", "requiredStatement", "logo", "seeAlso", "metadata", "items", "structures"})
public class ManifestVersion3 {

    @JsonProperty("@context")
    private List<String> context;

    public List<String> getContext() {
        return context;
    }

    public void setContext(final List<String> context) {
        this.context = context;
    }

    @JsonProperty("id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @JsonProperty("type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @JsonProperty
    private Map<String, List<String>> label;

    public Map<String, List<String>> getLabel() {
        return label;
    }

    public void setLabel(Map<String, List<String>> label) {
        this.label = label;
    }

    @JsonProperty
    private List<MetadataVersion3> metadata;

    public List<MetadataVersion3> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MetadataVersion3> metadata) {
        this.metadata = metadata;
    }

    @JsonProperty
    private MetadataVersion3 requiredStatement;

    public MetadataVersion3 getRequiredStatement() {
        return requiredStatement;
    }

    public void setRequiredStatement(MetadataVersion3 requiredStatement) {
        this.requiredStatement = requiredStatement;
    }

    @JsonProperty
    private Map<String, List<String>> summary;

    public Map<String, List<String>> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, List<String>> summary) {
        this.summary = summary;
    }

    @JsonProperty
    private String rights;

    public String getRights() {
        return rights;
    }

    public void setRights(final String rights) {
        this.rights = rights;
    }

    @JsonProperty
    private String viewingDirection;

    public String getViewingDirection() {
        return viewingDirection;
    }

    public void setViewingDirection(final String viewingDirection) {
        this.viewingDirection = viewingDirection;
    }

    @JsonProperty
    private List<String> behavior;

    public List<String> getBehavior() {
        return behavior;
    }

    public void setBehavior(final List<String> behavior) {
        this.behavior = behavior;
    }

    @JsonProperty
    private String navDate;

    public String getNavDate() {
        return navDate;
    }

    public void setNavDate(final String navDate) {
        this.navDate = navDate;
    }

    @JsonProperty
    private List<SeeAlso> seeAlso;

    public List<SeeAlso> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(final List<SeeAlso> seeAlso) {
        this.seeAlso = seeAlso;
    }

    @JsonProperty
    private Logo logo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"id", "type", "service"})
    public static class Logo {
        @JsonProperty("id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        @JsonProperty("type")
        private String type;

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        @JsonProperty
        private List<ServiceVersion3> service;

        public List<ServiceVersion3> getService() {
            return service;
        }

        public void setService(List<ServiceVersion3> service) {
            this.service = service;
        }
    }

    public Logo getLogo() {
        return logo;
    }

    public void setLogo(Logo logo) {
        this.logo = logo;
    }

    @JsonProperty
    private List<CanvasVersion3> items;

    public List<CanvasVersion3> getItems() {
        return items;
    }

    public void setItems(List<CanvasVersion3> items) {
        this.items = items;
    }

    @JsonProperty
    private List<Item> structures;

    public void setStructures(final List<Item> structures) {
        this.structures = structures;
    }

    public List<Item> getStructures() {
        return structures;
    }

}
