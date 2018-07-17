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

package de.ubleipzig.metadata.extractor.reserializer;

import static de.ubleipzig.metadata.extractor.ExtractorUtils.IIPSRV_DEFAULT;
import static de.ubleipzig.metadata.extractor.disassembler.DimensionManifestBuilder.mapServiceResponse;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.annotationBase;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.baseUrl;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.domainAttribution;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.domainLicense;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.domainLogo;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.katalogUrl;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.manifestBase;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.sequenceBase;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.targetBase;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.viewerUrl;
import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.iiif.vocabulary.IIIFEnum;
import de.ubleipzig.iiif.vocabulary.SC;
import de.ubleipzig.metadata.templates.Canvases;
import de.ubleipzig.metadata.templates.ImageServiceResponse;
import de.ubleipzig.metadata.templates.Images;
import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.Service;
import de.ubleipzig.metadata.templates.Structure;
import de.ubleipzig.metadata.templates.v2.Body;
import de.ubleipzig.metadata.templates.v2.Canvas;
import de.ubleipzig.metadata.templates.v2.PaintingAnnotation;
import de.ubleipzig.metadata.templates.v2.PerfectManifest;
import de.ubleipzig.metadata.templates.v2.Sequence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reserializer {

    private String body;
    private static final Logger LOGGER = LoggerFactory.getLogger(Reserializer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String xmldbHost;

    public Reserializer(final String body, final String xmldbHost) {
        this.body = body;
        this.xmldbHost = xmldbHost;
    }

    public String build() {
        try {
            final Manifest manifest = MAPPER.readValue(body, new TypeReference<Manifest>() {
            });

            final MetadataUtils metadataUtils = new MetadataBuilder(manifest, xmldbHost).build();

            //build structures objects
            final Optional<List<Structure>> structures = ofNullable(manifest.getStructures());
            final List<Canvas> canvases = new ArrayList<>();
            final String viewId = new URL(manifest.getId()).getPath().split(separator)[1];

            manifest.getSequences().forEach(sq -> {
                final AtomicInteger index = new AtomicInteger(1);
                for (Canvases c : sq.getCanvases()) {
                    Integer height = null;
                    Integer width = null;
                    final Canvas canvas = new Canvas();
                    final Body bodyObj = new Body();
                    for (Images i : c.getImages()) {
                        String iiifService = i.getResource().getService().getId();
                        //hack to fix service
                        if (iiifService.contains(IIPSRV_DEFAULT)) {
                            iiifService = iiifService.replace(IIPSRV_DEFAULT, "iiif");
                        }
                        if (!iiifService.contains("https")) {
                            iiifService = iiifService.replace("http", "https");
                        }

                        //getDimensionsFromImageService
                        InputStream is = null;
                        try {
                            is = new URL(iiifService + "/info.json").openStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final ImageServiceResponse ir = mapServiceResponse(is);
                        height = ir.getHeight();
                        width = ir.getWidth();

                        //createServiceObject
                        final Service service = new Service();
                        service.setContext(IIIFEnum.IMAGE_CONTEXT.IRIString());
                        service.setProfile(IIIFEnum.SERVICE_PROFILE.IRIString());
                        service.setId(iiifService);

                        //createBody
                        bodyObj.setService(service);
                        bodyObj.setResourceHeight(height);
                        bodyObj.setResourceWidth(width);
                        bodyObj.setResourceType("dctypes:Image");
                        bodyObj.setResourceFormat("image/jpeg");
                        String resourceId = i.getResource().getResourceId();
                        //hack for Mirador file extension check
                        if (resourceId.contains("jpx")) {
                            final String jpgResource = resourceId.replace("jpx", "jpg");
                            bodyObj.setResourceId(jpgResource);
                        } else {
                            bodyObj.setResourceId(resourceId);
                        }
                        bodyObj.setLabel(i.getResource().getLabel());
                    }
                    //createAnnotation
                    final String canvasId = baseUrl + viewId + separator + targetBase + separator + format(
                            "%08d", index.getAndIncrement());
                    final List<PaintingAnnotation> annotations = new ArrayList<>();
                    final PaintingAnnotation anno = new PaintingAnnotation();
                    final String annoId = baseUrl + viewId + separator + annotationBase + separator + UUID.randomUUID();
                    anno.setId(annoId);
                    anno.setBody(bodyObj);
                    anno.setTarget(canvasId);
                    annotations.add(anno);
                    canvas.setId(canvasId);
                    canvas.setImages(annotations);
                    canvas.setHeight(height);
                    canvas.setWidth(width);
                    canvas.setLabel(c.getLabel());
                    canvases.add(canvas);
                }
            });
            final List<Sequence> sequences = getSequence(canvases);
            final PerfectManifest perfectManifest = getManifest(viewId, sequences);
            if (structures.isPresent()) {
                final List<Structure> structs = structures.get();
                final StructureBuilder sbuilder = new StructureBuilder(structs, viewId, metadataUtils);
                sbuilder.fix();
                List<Structure> newStructures = sbuilder.build();
                perfectManifest.setStructures(newStructures);
            }
            final List<Metadata> finalMetadata = metadataUtils.getFinalMetadata();
            perfectManifest.setMetadata(finalMetadata);
            perfectManifest.setLabel(manifest.getLabel());
            final Optional<String> finalURN = ofNullable(getURNfromFinalMetadata(finalMetadata, viewId));
            final List<String> related = getRelated(viewId, finalURN.orElse(null));
            perfectManifest.setRelated(related);
            final Optional<String> json = serialize(perfectManifest);
            return json.orElse(null);
        } catch (IOException ex) {
            throw new RuntimeException("Could not Reserialize Manifest", ex.getCause());
        }
    }

    /**
     * @param graph graph
     * @return List
     */
    public List<Sequence> getSequence(final List<Canvas> graph) {
        final String id = baseUrl + sequenceBase + separator + UUID.randomUUID();
        final List<Sequence> sequences = new ArrayList<>();
        final Sequence sequence = new Sequence(id, graph);
        sequence.setViewingHint("paged");
        sequences.add(sequence);
        return sequences;
    }

    /**
     * @param sequences List
     * @return Manifest
     */
    public PerfectManifest getManifest(final String viewId, final List<Sequence> sequences) {
        final String id = baseUrl + viewId + separator + manifestBase + ".json";
        final PerfectManifest manifest = new PerfectManifest();
        manifest.setContext(SC.CONTEXT);
        manifest.setId(id);
        manifest.setLogo(domainLogo);
        manifest.setAttribution(domainAttribution);
        manifest.setLicense(domainLicense);
        manifest.setSequences(sequences);
        return manifest;
    }

    public List<String> getRelated(final String viewId, final String urn) {
        final ArrayList<String> related = new ArrayList<>();
        if (urn != null) {
            related.add(katalogUrl + urn);
        }
        related.add(viewerUrl + viewId);
        return related;
    }

    public String getURNfromFinalMetadata(final List<Metadata> finalMetadata, final String viewId) {
        final Metadata metaURN = finalMetadata.stream().filter(y -> y.getLabel().equals("URN")).findAny().orElse(null);
        if (metaURN != null) {
            return metaURN.getValue();
        } else {
            LOGGER.warn("No URN Available for {}", viewId);
        }
        return null;
    }
}
