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

package de.ubleipzig.metadata.extractor;

import static de.ubleipzig.metadata.extractor.Constants.annotationBase;
import static de.ubleipzig.metadata.extractor.Constants.baseUrl;
import static de.ubleipzig.metadata.extractor.Constants.domainAttribution;
import static de.ubleipzig.metadata.extractor.Constants.domainLicense;
import static de.ubleipzig.metadata.extractor.Constants.domainLogo;
import static de.ubleipzig.metadata.extractor.Constants.katalogUrl;
import static de.ubleipzig.metadata.extractor.Constants.manifestBase;
import static de.ubleipzig.metadata.extractor.Constants.sequenceBase;
import static de.ubleipzig.metadata.extractor.Constants.targetBase;
import static de.ubleipzig.metadata.extractor.Constants.viewerUrl;
import static de.ubleipzig.metadata.extractor.ExtractorUtils.IIPSRV_DEFAULT;
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
import java.net.MalformedURLException;
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

    public Reserializer(final String body) {
        this.body = body;
    }

    public List<Metadata> harmonizeMetadataLabels(List<Metadata> metadata) {
        metadata.forEach(m -> {
            String label = m.getLabel();
            switch (label) {
                case "urn":
                    m.setLabel("URN");
                    break;
                case "swb-ppn":
                    m.setLabel("Source PPN (SWB)");
                    break;
                case "goobi":
                    m.setLabel("Kitodo");
                    break;
                case "Callnumber":
                    m.setLabel("Call number");
                    break;
                case "Date":
                    m.setLabel("Date of publication");
                    break;
                case "datiert":
                    m.setLabel("Date of publication");
                    break;
                case "vd17":
                    m.setLabel("VD17");
                    break;
                case "Place":
                    m.setLabel("Place of publication");
                    break;
                case "Physical State":
                    m.setLabel("Physical description");
                    break;
                default:
                    break;
            }
        });
        return metadata;
    }

    public String build() {
        try {
            final Manifest manifest = MAPPER.readValue(body, new TypeReference<Manifest>() {
            });
            final Optional<List<Metadata>> metadata = ofNullable(manifest.getMetadata());
            String urn = null;
            List<Metadata> harmonizedMetadata = null;
            if (metadata.isPresent()) {
                harmonizedMetadata = harmonizeMetadataLabels(metadata.get());
                final Metadata metaURN = harmonizedMetadata.stream().filter(
                        y -> y.getLabel().equals("URN")).findAny().orElse(null);
                if (metaURN != null) {
                    urn = metaURN.getValue();
                }
            }
            //build structures objects
            final Optional<List<Structure>> structures = ofNullable(manifest.getStructures());
            final List<Canvas> canvases = new ArrayList<>();
            final String viewId = new URL(manifest.getId()).getPath().split(separator)[1];

            manifest.getSequences().forEach(sq -> {
                AtomicInteger index = new AtomicInteger(1);
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
                        bodyObj.setResourceId(i.getResource().getResourceId());
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
                structs.forEach(s -> {
                    final Optional<List<String>> cs = ofNullable(s.getCanvases());
                    final List<String> paddedCanvases = new ArrayList<>();
                    cs.ifPresent(x -> x.forEach(c -> {
                        try {
                            final String paddedCanvasId = format(
                                    "%08d", Integer.valueOf(new URL(c).getPath().split(separator)[3]));
                            final String canvas = baseUrl + viewId + separator + targetBase + separator +
                                    paddedCanvasId;
                            paddedCanvases.add(canvas);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }));
                    if (!paddedCanvases.isEmpty()) {
                        s.setCanvases(paddedCanvases);
                    }
                });
                //structs.sort(comparing(Structure::getStructureId));
                perfectManifest.setStructures(structs);
            }
            perfectManifest.setMetadata(harmonizedMetadata);
            perfectManifest.setLabel(manifest.getLabel());
            List<String> related = getRelated(perfectManifest.getId(), viewId, urn);
            perfectManifest.setRelated(related);
            final Optional<String> json = serialize(perfectManifest);
            return json.orElse(null);
        } catch (IOException ex) {
            throw new RuntimeException("Could not Reserialize Manifest", ex.getCause());
        }
    }

    /**
     * @param is InputStream
     * @return ImageServiceResponse
     */
    public ImageServiceResponse mapServiceResponse(final InputStream is) {
        try {
            return MAPPER.readValue(is, new TypeReference<ImageServiceResponse>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
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

    public List<String> getRelated(final String manifestId, final String viewId, final String urn) {
        final ArrayList<String> related = new ArrayList<>();
        if (urn != null) {
            related.add(katalogUrl + urn);
        }
        related.add(viewerUrl + viewId);
        related.add(manifestId);
        return related;
    }
}
