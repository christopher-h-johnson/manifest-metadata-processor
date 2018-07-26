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
package de.ubleipzig.metadata.templates.atomic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ubleipzig.metadata.templates.ContentList;
import de.ubleipzig.metadata.templates.v2.Structure;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnnotationBodyAtom {

    @JsonProperty
    private String iiifService;

    @JsonProperty
    private Integer imageIndex;

    @JsonProperty
    private Map<String, String> metadata;

    @JsonProperty
    private Map<Integer, Structure> structureMap;

    @JsonProperty
    private List<ContentList.Content> contentList;

    public String getIiifService() {
        return iiifService;
    }

    public void setIiifService(String iiifService) {
        this.iiifService = iiifService;
    }

    public Integer getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(Integer imageIndex) {
        this.imageIndex = imageIndex;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<Integer, Structure> getStructureMap() {
        return structureMap;
    }

    public void setStructureMap(Map<Integer, Structure> structureMap) {
        this.structureMap = structureMap;
    }

    public List<ContentList.Content> getContentList() {
        return contentList;
    }

    public void setContentList(List<ContentList.Content> contentList) {
        this.contentList = contentList;
    }
}
