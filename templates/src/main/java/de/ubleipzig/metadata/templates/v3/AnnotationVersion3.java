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
package de.ubleipzig.metadata.templates.v3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "type", "motivation", "body", "target"})
public class AnnotationVersion3 {
    @JsonProperty("@context")
    private List<String> context;

    public List<String> getContext() {
        return context;
    }

    public void setContext(final List<String> context) {
        this.context = context;
    }

    @JsonProperty("id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @JsonProperty("type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @JsonProperty("motivation")
    private String motivation;

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(final String motivation) {
        this.motivation = motivation;
    }

    @JsonProperty("target")
    private String target;

    public String getTarget() {
        return target;
    }

    @JsonProperty("body")
    private BodyVersion3 body;

    public BodyVersion3 getBody() {
        return body;
    }

    public void setBody(final BodyVersion3 body) {
        this.body = body;
    }

    public void setTarget(final String target) {
        this.target = target;
    }
}
