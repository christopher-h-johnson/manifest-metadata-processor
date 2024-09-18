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
package de.ubleipzig.metadata.producer;

import static java.io.File.separator;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;
import static java.lang.String.format;

import java.io.File;
import java.util.UUID;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class IRIBuilder {
    private final Config config;
    private static final RDF rdf = new SimpleRDF();

    /**
     * @param config Config
     */
    public IRIBuilder(final Config config) {
        this.config = config;
    }
    /**
     * @return String
     */
    public String buildImageServiceContext(final String viewId) {
        final int viewIdInt = parseInt(viewId);
        final String v = format("%010d", viewIdInt);
        final String imageDirPrefix = config.getImageServiceImageDirPrefix();
        final int part1 = parseInt(v.substring(0,4));
        final String first = format("%04d", part1);
        final int part2 = parseInt(v.substring(5,8));
        final String second = format("%04d", part2);
        if (config.getIsUBLImageService()) {
            return config.getImageServiceBaseUrl() + imageDirPrefix + first + separator + second + separator + v;
        } else {
            return config.getImageServiceBaseUrl() + viewId;
        }
    }

    /**
     * @param imageServiceContext String
     * @param resourceIdString String
     * @return IRI
     */
    public IRI buildServiceIRI(final String imageServiceContext, final String resourceIdString) {
        return rdf.createIRI(
                imageServiceContext + separator + resourceIdString + config.getImageServiceFileExtension());
    }

    /**
     * @return String
     */
    public String buildAnnotationId(final String resourceContext) {
        return resourceContext + config.getAnnotationContext() + File.separator + UUID.randomUUID();
    }

    /**
     * @return String
     */
    public String buildSequenceId(final String resourceContext) {
        return resourceContext + config.getSequenceContext() + File.separator + UUID.randomUUID();
    }

    /**
     * @param physical String
     * @return String
     */
    public String buildCanvasIRIfromPhysical(final String physical, final String resourceContext) {
        final Integer newId = valueOf(physical.substring(physical.indexOf("_") + 1));
        return resourceContext + config.getCanvasContext() + File.separator + format("%08d", newId);
    }
}
