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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ImageServiceResponse {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty
    private String protocol;

    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return Integer
      * @param width Integer
     */
    @Setter
    @Getter
    @JsonProperty
    private Integer width;

    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return Integer
      * @param height Integer
     */
    @Setter
    @Getter
    @JsonProperty
    private Integer height;

    @JsonProperty
    private List<Object> sizes;

    @JsonProperty
    private List<Object> profile;

    @JsonProperty
    private List<Object> tiles;

}
