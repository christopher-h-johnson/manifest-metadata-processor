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

package de.ubleipzig.metadata.templates.metsmods;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.List;

public class RecordList {
    @JsonProperty
    private List<Record> records;

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<RecordList.Record> records) {
        this.records = records;
    }

    public static class Record {
        @Setter
        @JsonProperty
        private String urn;

        public String getUrn() {
            return urn;
        }

        @JsonProperty
        private String filename;

        public String getFilename() {
            return filename;
        }

        @JsonProperty
        private String createDate;

        public String getCreateDate() {
            return createDate;
        }

        @Setter
        @JsonProperty
        private String resolvedURI;

        public String getResolvedURI() {
            return resolvedURI;
        }
    }
}
