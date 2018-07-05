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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value={"format", "height", "width", "@type","type"}, allowGetters=true)
public class Resource {

    @JsonProperty
    private Service service;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty
    private String label;

    @JsonProperty
    private String format;

    @JsonProperty
    private String height;

    @JsonProperty
    private String width;

    /**
     * getResourceId.
     *
     * @return String
     */
    @JsonIgnore
    public String getResourceId() {
        return this.id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getHeight() {
        return height;
    }

    public String getWidth() {
        return width;
    }

    public String getLabel() {
        return label;
    }
}
