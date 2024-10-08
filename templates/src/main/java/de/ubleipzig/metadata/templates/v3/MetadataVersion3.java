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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class MetadataVersion3 {

    @Setter
    @Getter
    @JsonProperty
    private Map<String, List<String>> label;

    @Setter
    @Getter
    @JsonProperty
    private Map<String, List<String>> value;

    @JsonIgnore
    private Integer displayOrder;

    /**
     * @return Integer
     */
    @JsonIgnore
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * @param displayOrder Integer
     */
    @JsonIgnore
    public void setDisplayOrder(final Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

}
