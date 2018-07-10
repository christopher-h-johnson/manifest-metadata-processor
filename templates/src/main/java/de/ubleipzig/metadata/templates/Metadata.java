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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Metadata.
 *
 * @author christopher-johnson
 */
@JsonPropertyOrder({"label", "value"})
public class Metadata {

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private String value;

    @JsonIgnore
    private Integer displayOrder;


    /**
     * @return String
     */
    @JsonIgnore
    public String getLabel() {
        return label;
    }

    /**
     * @param label String
     */
    @JsonIgnore
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getValue() {
        return value;
    }

    /**
     * @param value String
     */
    @JsonIgnore
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * @param displayOrder String
     */
    @JsonIgnore
    public void setDisplayOrder(final Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

}