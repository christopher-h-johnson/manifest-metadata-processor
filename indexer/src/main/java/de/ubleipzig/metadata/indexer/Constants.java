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
package de.ubleipzig.metadata.indexer;

public final class Constants {

    private Constants() {
    }

    static final String elasticSearchHost = "http://localhost:9100/";
    static final String scannerAPIHost = "http://localhost:9085/scanner";
    static final String lineSeparator = "line.separator";
    static final String docTypeIndex = "_doc";
    static final String bulkContext = "_bulk";
    static final String contentTypeJson = "application/json";
}
