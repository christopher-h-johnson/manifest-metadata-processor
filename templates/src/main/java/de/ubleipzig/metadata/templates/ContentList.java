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

import java.util.List;

public class ContentList {
    @JsonProperty
    private List<Content> contentList;

    @JsonIgnore
    public List<Content> getContentList() {
        return contentList;
    }

    public void setContentList(List<Content> contentList) {
        this.contentList = contentList;
    }

    public static class Content {
        @JsonProperty
        private String objectId;

        @JsonProperty
        private String chars;

        @JsonProperty
        private String region;

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(final String objectId) {
            this.objectId = objectId;
        }

        public String getChars() {
            return chars;
        }

        public void setChars(final String chars) {
            this.chars = chars;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(final String region) {
            this.region = region;
        }
    }
}
