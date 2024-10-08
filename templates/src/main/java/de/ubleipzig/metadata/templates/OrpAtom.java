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
import lombok.Getter;

import java.util.Map;

public class OrpAtom {

    @JsonProperty
    private Integer imageIndex;

    @JsonProperty
    private String imageServiceIRI;

    /**
     * -- GETTER --
     *  getMetadataMap.
     *
     * @return Map
     */
    @Getter
    @JsonProperty
    private Map<String, String> metadataMap;

    /**
     * getImageIndex.
     *
     * @return Integer
     */
    @JsonIgnore
    public Integer getImageIndex() {
        return imageIndex;
    }

    /**
     * setImageIndex.
     *
     * @param imageIndex Integer
     */
    public void setImageIndex(Integer imageIndex) {
        this.imageIndex = imageIndex;
    }

    /**
     * getImageServiceIRI.
     *
     * @return String
     */
    @JsonIgnore
    public String getImageServiceIRI() {
        return imageServiceIRI;
    }

    /**
     * setImageServiceIRI.
     *
     * @param imageServiceIRI String
     */
    public void setImageServiceIRI(String imageServiceIRI) {
        this.imageServiceIRI = imageServiceIRI;
    }

    /**
     * setMetadataMap.
     *
     * @param metadataMap Map
     */
    public void setMetadataMap(final Map<String, String> metadataMap) {
        this.metadataMap = metadataMap;
    }
}
