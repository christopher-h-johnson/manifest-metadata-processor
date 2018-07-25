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

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static de.ubleipzig.metadata.producer.doc.MetsConstants.URN_TYPE;
import static de.ubleipzig.metadata.producer.doc.MetsManifestBuilder.getManuscriptIdByType;
import static de.ubleipzig.metadata.producer.doc.MetsManifestBuilder.getMetsFromString;
import static de.ubleipzig.metadata.producer.doc.MetsManifestBuilder.getOrderLabelForDiv;
import static de.ubleipzig.metadata.producer.doc.MetsManifestBuilder.getPhysicalDivs;
import static de.ubleipzig.metadata.producer.doc.MetsManifestBuilder.getPresentationUri;
import static java.io.File.separator;
import static java.lang.String.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.ubleipzig.iiif.vocabulary.SC;
import de.ubleipzig.metadata.producer.doc.MetsData;
import de.ubleipzig.metadata.templates.ImageServiceResponse;
import de.ubleipzig.metadata.templates.Service;
import de.ubleipzig.metadata.templates.v2.Body;
import de.ubleipzig.metadata.templates.v2.Canvas;
import de.ubleipzig.metadata.templates.v2.PaintingAnnotation;
import de.ubleipzig.metadata.templates.v2.PerfectManifest;
import de.ubleipzig.metadata.templates.v2.Sequence;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerBuilderVersion2 {

    private String body;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerBuilderVersion2.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String xmldbHost;
    private MetsData mets;

    public ProducerBuilderVersion2(final String body, final String xmldbHost) {
        this.body = body;
        this.xmldbHost = xmldbHost;
        this.mets = getMetsFromString(body);
    }

    /**
     * This method parses the provided configFile into its equivalent command-line args.
     *
     * @param configFile containing config args
     * @return Array of args
     */
    private Config retrieveConfig(final InputStream configFile) {
        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(configFile, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getViewId(final String presentationUri) {
        try {
            final String presentationUriPath = new URL(presentationUri).getPath();
            final String[] parts = presentationUriPath.split("/");
            return parts[3];
        } catch (MalformedURLException e) {
            throw new RuntimeException("No view Id found. Exiting " + e.getMessage());
        }
    }

    public String build() {
        final PerfectManifest manifest = new PerfectManifest();
        manifest.setContext(SC.CONTEXT);
        final Config config = retrieveConfig(ProducerBuilderVersion2.class.getResourceAsStream("/producer-config.yml"));
        final IRIBuilder iriBuilder = new IRIBuilder(config);
        final String presentationUri = getPresentationUri(mets);
        final String viewId = getViewId(presentationUri);
        final String imageServiceContext = iriBuilder.buildImageServiceContext(viewId);
        final String canvasContext = config.getCanvasContext();
        final String baseUrl = config.getBaseUrl();
        final String resourceContext = baseUrl + viewId;
        manifest.setId(resourceContext + separator + config.getManifestFilename());

        //setRelated
        final ArrayList<String> related = new ArrayList<>();
        final String urn = getManuscriptIdByType(mets, URN_TYPE);
        related.add(config.getKatalogUrl() + urn);
        related.add(presentationUri);
        manifest.setRelated(related);

        final List<Canvas> canvases = new ArrayList<>();
        final AtomicInteger atomicInteger = new AtomicInteger(1);
        final List<String> divs = getPhysicalDivs(mets);
        for (String div : divs) {
            final String label = getOrderLabelForDiv(mets, div);
            final Canvas canvas = new Canvas();
            canvas.setLabel(label);
            final Body body = new Body();
            body.setLabel(label);

            //buildServiceIRI
            final String resourceFileId = format("%08d", atomicInteger.getAndIncrement());
            final IRI serviceIRI = iriBuilder.buildServiceIRI(imageServiceContext, resourceFileId);

            //canvasId = resourceId
            final String canvasIdString = resourceContext + canvasContext + separator + resourceFileId;
            final String bodyIdString = resourceContext + separator + resourceFileId + ".jpg";
            //set Canvas Id
            canvas.setId(canvasIdString);
            //set BodyId
            body.setResourceId(bodyIdString);

            //getDimensionsFromImageService
            InputStream is = null;
            try {
                is = new URL(serviceIRI.getIRIString() + "/info.json").openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            final ImageServiceResponse ir = mapServiceResponse(is);
            final Integer height = ir.getHeight();
            final Integer width = ir.getWidth();
            canvas.setWidth(width);
            canvas.setHeight(height);
            body.setResourceWidth(width);
            body.setResourceHeight(height);

            //build Service
            final Service service = new Service();
            service.setId(serviceIRI.getIRIString());
            service.setContext(config.getImageServiceContext());
            service.setProfile(config.getImageServiceProfile());
            body.setService(service);

            //build Annotation
            final PaintingAnnotation anno = new PaintingAnnotation();
            final String annotationId = iriBuilder.buildAnnotationId(resourceContext);
            anno.setId(annotationId);
            anno.setBody(body);
            anno.setTarget(canvasIdString);
            final List<PaintingAnnotation> images = new ArrayList<>();
            images.add(anno);
            canvas.setImages(images);
            canvases.add(canvas);
        }
        final String sequenceId = iriBuilder.buildSequenceId(resourceContext);
        final List<Sequence> sequence = addCanvasesToSequence(canvases, sequenceId);
        manifest.setSequences(sequence);
        LOGGER.info("Builder Process Complete, Serializing to Json ...");
        final Optional<String> json = serialize(manifest);
        return json.orElse(null);
    }

    public List<Sequence> addCanvasesToSequence(final List<Canvas> canvases, final String sequenceId) {
        final List<Sequence> sequences = new ArrayList<>();
        final Sequence sequence = new Sequence();
        sequence.setId(sequenceId);
        sequence.setCanvases(canvases);
        sequences.add(sequence);
        return sequences;
    }

    /**
     * @param res String
     * @return ImageServiceResponse
     */
    public static ImageServiceResponse mapServiceResponse(final InputStream res) {
        try {
            return MAPPER.readValue(res, new TypeReference<ImageServiceResponse>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
