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

package de.ubleipzig.metadata.templates.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.ubleipzig.iiif.vocabulary.SCEnum;

import java.util.List;

/**
 * PaintingAnnotation.
 *
 * @author christopher-johnson
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"@context", "id", "type", "motivation", "body", "target"})
public class PaintingAnnotation {

    @JsonProperty("@context")
    private List<String> context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty
    private String label;

    @JsonProperty("@type")
    private String type = "oa:Annotation";

    @JsonProperty
    private String motivation = SCEnum.painting.compactedIRI();

    @JsonProperty
    private Body resource;

    @JsonProperty
    private String on;

    /**
     * setContext.
     *
     * @param context a {@link List} of contexts
     */
    public void setContext(final List<String> context) {
        this.context = context;
    }

    /**
     * setId.
     *
     * @param id id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * getAnnoId.
     *
     * @return id
     */
    @JsonIgnore
    public String getAnnoId() {
        return this.id;
    }

    /**
     * getBody.
     *
     * @return TemplateBody
     */
    @JsonIgnore
    public Body getBody() {
        return this.resource;
    }

    /**
     * setBody.
     *
     * @param resource body
     */
    public void setBody(final Body resource) {
        this.resource = resource;
    }

    /**
     * setTarget.
     *
     * @param on target
     */
    public void setTarget(final String on) {
        this.on = on;
    }

}
