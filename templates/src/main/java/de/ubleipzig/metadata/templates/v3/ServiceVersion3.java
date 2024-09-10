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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"id", "type", "profile"})
public class ServiceVersion3 {
    @Setter
    @Getter
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    /**
     * @return String
     */
    @JsonIgnore
    public String getType() {
        return this.type;
    }

    /**
     * @param type type
     */
    @JsonIgnore
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * -- SETTER --
     *
     * @param profile profile
     */
    @Setter
    @JsonProperty
    private String profile;

    /**
     * @return String
     */
    @JsonIgnore
    public String getProfile() {
        return this.profile;
    }

}
