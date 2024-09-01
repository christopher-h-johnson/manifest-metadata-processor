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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"@context", "@id", "profile"})
public class Service {

    @JsonIgnoreProperties({"profile"})

    @JsonProperty("@context")
    private String context;

    @Getter
    @Setter
    @JsonProperty("@id")
    private String id;

    @JsonProperty
    private Integer height;

    @JsonProperty
    private Integer width;

    /**
     * -- SETTER --
     *
     * @param profile profile
     */
    @Setter
    @JsonProperty
    private String profile;

    @JsonProperty
    private String protocol;

    @JsonProperty
    private Object tiles;

    /**
     * @return String
     */
    @JsonIgnore
    public String getContext() {
        return this.context;
    }

    /**
     * @param context context
     */
    @JsonIgnore
    public void setContext(final String context) {
        this.context = context;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getProfile() {
        return this.profile;
    }

}
