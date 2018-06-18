package de.ubleipzig.metadata.indexer;

public final class Constants {

    private Constants() {
    }

    static final String elasticSearchHost = "http://localhost:9100/";
    static final String lineSeparator = "line.separator";
    static final String docTypeIndex = "_doc";
    static final String bulkContext = "_bulk";
    static final String contentTypeJson = "application/json";
}
