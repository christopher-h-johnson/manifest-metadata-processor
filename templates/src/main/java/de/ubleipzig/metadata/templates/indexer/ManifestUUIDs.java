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
package de.ubleipzig.metadata.templates.indexer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ManifestUUIDs {

    @JsonProperty
    private Map<String, Object> manifestMap;

    public Map<String,Object> getManifestMap() {
        return manifestMap;
    }

    public void setManifestMap(final Map<String, Object> manifestMap) {
        this.manifestMap = manifestMap;
    }

}
