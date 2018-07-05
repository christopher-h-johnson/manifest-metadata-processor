package de.ubleipzig.metadata.templates.metsmods;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RecordList {
    @JsonProperty
    private List<Record> records;

    public List<Record> getRecords()  {
        return records;
    }

    public static class Record {
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
    }
}
