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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.ubleipzig.iiif.vocabulary.SC;
import de.ubleipzig.metadata.producer.doc.MetsData;
import de.ubleipzig.metadata.templates.ImageServiceResponse;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.Service;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;
import de.ubleipzig.metadata.templates.v2.*;
import de.ubleipzig.metadata.transformer.MetadataImplVersion2;
import de.ubleipzig.metadata.transformer.XmlDbAccessor;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static de.ubleipzig.metadata.producer.doc.MetsConstants.URN_TYPE;
import static de.ubleipzig.metadata.producer.doc.MetsManifestBuilder.*;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class ProducerBuilderVersion2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerBuilderVersion2.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String xmldbHost;
    private MetsData mets;

    public ProducerBuilderVersion2(final String xmlDoc, final String xmldbHost) {
        this.xmldbHost = xmldbHost;
        this.mets = getMetsFromString(xmlDoc);
    }

    /**
     * retrieveConfig.
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
            throw new RuntimeException("Could not read configuration " + e.getMessage());
        }
    }

    /**
     * getViewId.
     *
     * @param presentationUri the location of the viewer
     * @return the 10 digit viewId integer as a String
     */
    private String getViewId(final String presentationUri) {
        try {
            final String presentationUriPath = new URL(presentationUri).getPath();
            final String[] parts = presentationUriPath.split(separator);
            return parts[3];
        } catch (MalformedURLException e) {
            throw new RuntimeException("No view Id found. Exiting " + e.getMessage());
        }
    }

    /**
     * build.
     *
     * @return a json-ld manifest as a String
     */
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

        //setTitle
        final String title = getManifestTitle(mets);
        manifest.setLabel(title);

        //setLicense
        manifest.setLicense(config.getLicense());

        //setAttribution
        final String attribution = config.getAttributionKey() + getAttribution(
                mets) + "<br/>" + config.getAttributionLicenseNote();
        manifest.setAttribution(attribution);

        //setLogo
        manifest.setLogo(getLogo(mets));

        //setRelated
        final ArrayList<String> related = new ArrayList<>();
        final String urn = getManuscriptIdByType(mets, URN_TYPE);
        related.add(config.getKatalogUrl() + urn);
        related.add(presentationUri);
        manifest.setRelated(related);

        //getMetadataFromJSON_API
        final MetadataImplVersion2 metadataImplVersion2 = new MetadataImplVersion2();
        final XmlDbAccessor accessor = new XmlDbAccessor(xmldbHost);
        final MetsMods metsmods = accessor.getMetadataFromAPI(urn);
        metadataImplVersion2.setMetsMods(metsmods);
        metadataImplVersion2.buildFinalMetadata();
        final List<Metadata> metadata = metadataImplVersion2.getMetadata();
        manifest.setMetadata(metadata);

        //build Structures
        final StructureBuilderVersion2 structureBuilderVersion2 = new StructureBuilderVersion2(
                config, mets, resourceContext);
        final Optional<StructureList> structureList = ofNullable(structureBuilderVersion2.build());
        structureList.ifPresent(slist -> manifest.setStructures(slist.getStructureList()));

        //buildCanvases
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
            final String bodyIdString =
                    resourceContext + separator + resourceFileId + config.getResourceFileExtension();
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
            body.setResourceType(config.getResourceType());
            body.setResourceFormat(config.getResourceFormat());
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
        final List<Sequence> sequence = addCanvasesToSequence(canvases, sequenceId, config);
        manifest.setSequences(sequence);

        LOGGER.info("Builder Process Complete, Serializing to Json ...");
        final Optional<String> json = serialize(manifest);
        return json.orElse(null);
    }

    /**
     * addCanvasesToSequence.
     *
     * @param canvases   List
     * @param sequenceId String
     * @return List
     */
    public List<Sequence> addCanvasesToSequence(final List<Canvas> canvases, final String sequenceId,
                                                final Config config) {
        final List<Sequence> sequences = new ArrayList<>();
        final Sequence sequence = new Sequence();
        sequence.setId(sequenceId);
        sequence.setCanvases(canvases);
        sequence.setViewingHint(config.getViewingHint());
        sequences.add(sequence);
        return sequences;
    }

    /**
     * mapServiceResponse.
     *
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
