package de.ubleipzig.metadata.producer;

import static java.io.File.separator;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import java.io.File;
import java.util.UUID;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class IRIBuilder {
    private Config config;
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
}
