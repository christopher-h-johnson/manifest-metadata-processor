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
@JsonPropertyOrder({"id", "type", "label", "height", "width", "items"})
public class CanvasVersion3 {
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
    private List<AnnotationPage> items;

    public List<AnnotationPage> getItems() {
        return items;
    }

    public void setItems(List<AnnotationPage> items) {
        this.items = items;
    }

    @JsonProperty
    private Integer height;

    public Integer getHeight() {
        return height;
    }

    public void setHeight(final Integer height) {
        this.height = height;
    }

    @JsonProperty
    private Integer width;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(final Integer width) {
        this.width = width;
    }
}
