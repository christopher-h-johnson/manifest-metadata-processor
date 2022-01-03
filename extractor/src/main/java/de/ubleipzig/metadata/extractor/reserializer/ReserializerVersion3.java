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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ubleipzig.metadata.templates.Canvases;
import de.ubleipzig.metadata.templates.ImageServiceResponse;
import de.ubleipzig.metadata.templates.Images;
import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.v2.Structure;
import de.ubleipzig.metadata.templates.v3.*;
import de.ubleipzig.metadata.transformer.MetadataApi;
import de.ubleipzig.metadata.transformer.MetadataBuilderVersion3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.ubleipzig.metadata.extractor.ExtractorUtils.IIPSRV_DEFAULT;
import static de.ubleipzig.metadata.extractor.disassembler.DimensionManifestBuilder.mapServiceResponse;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.*;
import static de.ubleipzig.metadata.extractor.reserializer.ReserializerUtils.buildLabelMap;
import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class ReserializerVersion3 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReserializerVersion3.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static String NONE = "@none";
    private String body;
    private String xmldbHost;

    public ReserializerVersion3(final String body, final String xmldbHost) {
        this.body = body;
        this.xmldbHost = xmldbHost;
    }

    public String build() {
        try {
            final Manifest manifest = MAPPER.readValue(body, new TypeReference<Manifest>() {
            });
            MetadataApi<MetadataVersion3> metadataImplVersion3 = new MetadataBuilderVersion3(
                    manifest, xmldbHost).build();
            final List<CanvasVersion3> canvases = new ArrayList<>();
            final String viewId = new URL(manifest.getId()).getPath().split(separator)[1];

            manifest.getSequences().forEach(sq -> {
                final AtomicInteger index = new AtomicInteger(1);
                for (Canvases c : sq.getCanvases()) {
                    Integer height = null;
                    Integer width = null;
                    final CanvasVersion3 canvas = new CanvasVersion3();
                    final BodyVersion3 body = new BodyVersion3();
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

                        final ServiceVersion3 service = new ServiceVersion3();
                        service.setId(iiifService);
                        service.setType(IIIF_SERVICE_TYPE);
                        service.setProfile(IIIF_SERVICE_PROFILE);
                        final List<ServiceVersion3> services = new ArrayList<>();
                        services.add(service);

                        //createBody
                        body.setService(services);
                        body.setHeight(height);
                        body.setWidth(width);
                        body.setType("Image");
                        body.setFormat("image/jpeg");
                        String resourceId = i.getResource().getResourceId();
                        //hack for Mirador file extension check
                        if (resourceId.contains("jpx")) {
                            final String jpgResource = resourceId.replace("jpx", "jpg");
                            body.setId(jpgResource);
                        } else {
                            body.setId(resourceId);
                        }
                        //build Body label
                        final String bodyLabel = i.getResource().getLabel();
                        final Map<String, List<String>> bodyLabelMap = buildLabelMap(bodyLabel, NONE);
                        body.setLabel(bodyLabelMap);
                    }
                    //createAnnotation
                    final String canvasId = baseUrl + viewId + separator + targetBase + separator + format(
                            "%08d", index.getAndIncrement());
                    final List<AnnotationVersion3> annotations = new ArrayList<>();
                    final AnnotationVersion3 anno = new AnnotationVersion3();
                    final String annoId = baseUrl + viewId + separator + annotationBase + separator + UUID.randomUUID();
                    anno.setId(annoId);
                    anno.setType("Annotation");
                    anno.setMotivation("painting");
                    anno.setBody(body);
                    anno.setTarget(canvasId);
                    annotations.add(anno);

                    //createAnnotationPage
                    final List<AnnotationPage> annoPages = new ArrayList<>();
                    final AnnotationPage annoPage = new AnnotationPage();
                    final String annoPageId =
                            baseUrl + viewId + separator + annotationPageBase + separator + UUID.randomUUID();
                    annoPage.setId(annoPageId);
                    annoPage.setType("AnnotationPage");
                    annoPage.setItems(annotations);
                    annoPages.add(annoPage);

                    //setCanvas
                    canvas.setId(canvasId);
                    canvas.setType("Canvas");
                    canvas.setItems(annoPages);
                    canvas.setHeight(height);
                    canvas.setWidth(width);
                    final String canvasLabel = c.getLabel();
                    final Map<String, List<String>> canvasLabelMap = buildLabelMap(canvasLabel, NONE);
                    canvas.setLabel(canvasLabelMap);
                    canvases.add(canvas);
                }
            });

            List<MetadataVersion3> finalMetadata = metadataImplVersion3.getMetadata();
            ManifestVersion3 newManifest = buildManifest(viewId, canvases);

            //build structures objects
            final Optional<List<Structure>> structures = ofNullable(manifest.getStructures());
            if (structures.isPresent()) {
                final List<Structure> structs = structures.get();
                final StructureBuilderVersion3 sbuilder = new StructureBuilderVersion3(
                        structs, viewId, metadataImplVersion3);
                sbuilder.fix();
                List<Item> newStructures = sbuilder.build();
                newManifest.setStructures(newStructures);
            }

            //set seeAlso
            final Optional<String> finalURN = ofNullable(getURNfromFinalMetadata(finalMetadata, viewId));
            final List<SeeAlso> seeAlso = setSeeAlso(viewId, finalURN.orElse(null));
            newManifest.setSeeAlso(seeAlso);
            newManifest.setMetadata(finalMetadata);

            //set manifest label
            final String manifestLabel = manifest.getLabel();
            final Map<String, List<String>> manifestLabelMap = buildLabelMap(manifestLabel, NONE);
            newManifest.setLabel(manifestLabelMap);

            final Optional<String> json = serialize(newManifest);
            return json.orElse(null);
        } catch (IOException ex) {
            throw new RuntimeException("Could not Reserialize Manifest", ex.getCause());
        }
    }

    public ManifestVersion3 buildManifest(final String viewId, final List<CanvasVersion3> canvases) {
        final ManifestVersion3 newManifest = new ManifestVersion3();
        final List<String> contexts = new ArrayList<>();
        contexts.add(WEB_ANNOTATION_CONTEXT);
        contexts.add(IIIF_VERSION3_CONTEXT);
        newManifest.setContext(contexts);
        final String id = baseUrl + viewId + separator + manifestBase + ".json";
        newManifest.setId(id);
        newManifest.setType("Manifest");
        newManifest.setViewingDirection("left-to-right");
        final List<String> behaviors = new ArrayList<>();
        behaviors.add("paged");
        newManifest.setBehavior(behaviors);
        newManifest.setRights(domainLicense);
        final MetadataVersion3 requiredStatement = new MetadataVersion3();
        final Map<String, List<String>> label = buildLabelMap("Attribution", "en");
        final Map<String, List<String>> value = buildLabelMap(domainAttribution, "en");
        requiredStatement.setLabel(label);
        requiredStatement.setValue(value);
        newManifest.setRequiredStatement(requiredStatement);
        newManifest.setItems(canvases);
        final ManifestVersion3.Logo logo = new ManifestVersion3.Logo();
        logo.setId(domainLogo);
        logo.setType("Image");
        newManifest.setLogo(logo);
        return newManifest;
    }

    public List<SeeAlso> setSeeAlso(final String viewId, final String urn) {
        final ArrayList<SeeAlso> seeAlso = new ArrayList<>();
        if (urn != null) {
            final SeeAlso katalogReference = new SeeAlso();
            katalogReference.setId(katalogUrl + urn);
            katalogReference.setFormat("text/html");
            katalogReference.setType("Application");
            katalogReference.setProfile(SEE_ALSO_PROFILE);
            seeAlso.add(katalogReference);
        }
        final SeeAlso viewerReference = new SeeAlso();
        viewerReference.setId(viewerUrl + viewId);
        viewerReference.setFormat("text/html");
        viewerReference.setType("Application");
        viewerReference.setProfile(SEE_ALSO_PROFILE);
        seeAlso.add(viewerReference);
        return seeAlso;
    }

    public String getURNfromFinalMetadata(final List<MetadataVersion3> finalMetadata, final String viewId) {
        final Optional<Set<MetadataVersion3>> metaURN = Optional.of(finalMetadata.stream().filter(
                y -> y.getLabel().values().stream().anyMatch(v -> v.contains("URN"))).collect(Collectors.toSet()));
        if (metaURN.isPresent()) {
            final Optional<MetadataVersion3> urn = metaURN.get().stream().findAny();
            if (urn.isPresent()) {
                return urn.get().getValue().get(NONE).get(0);
            }
        } else {
            LOGGER.warn("No URN Available for {}", viewId);
        }
        return null;
    }
}
