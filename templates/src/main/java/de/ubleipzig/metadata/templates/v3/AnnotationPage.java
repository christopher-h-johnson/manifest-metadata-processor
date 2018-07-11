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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"id", "type", "items"})
public class AnnotationPage {
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
    private List<AnnotationVersion3> items;

    public List<AnnotationVersion3> getItems() {
        return items;
    }

    public void setItems(List<AnnotationVersion3> items) {
        this.items = items;
    }
}
