package de.ubleipzig.metadata.templates.collections;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class TopCollection {

    @JsonProperty("@context")
    private String context;

    @Setter
    @Getter
    @JsonProperty("@id")
    private String id;

    @Setter
    @Getter
    @JsonProperty("@type")
    private String type;

    @Setter
    @Getter
    @JsonProperty
    private String label;

    @Setter
    @Getter
    @JsonProperty
    private List<PagedCollection> collections;

    @Setter
    @Getter
    @JsonProperty
    private String description;

    @Setter
    @Getter
    @JsonProperty
    private String logo;

    @Setter
    @Getter
    @JsonProperty
    private String attribution;

    @Setter
    @Getter
    @JsonProperty
    private List<PagedCollection> members;

}

