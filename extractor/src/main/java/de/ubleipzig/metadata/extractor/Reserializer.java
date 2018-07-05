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
import static de.ubleipzig.metadata.extractor.Constants.structureBase;
import static de.ubleipzig.metadata.extractor.Constants.targetBase;
import static de.ubleipzig.metadata.extractor.Constants.viewerUrl;
import static de.ubleipzig.metadata.extractor.ExtractorUtils.IIPSRV_DEFAULT;
import static de.ubleipzig.metadata.extractor.MetadataUtils.harmonizeMetadataLabels;
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
import de.ubleipzig.metadata.templates.metsmods.MetsMods;
import de.ubleipzig.metadata.templates.metsmods.RecordList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

public class Reserializer {

    private String body;
    private final LdpClient client = new LdpClientImpl();
    private static final Logger LOGGER = LoggerFactory.getLogger(Reserializer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JenaRDF rdf = new JenaRDF();
    private MetadataUtils metadataUtils = new MetadataUtils();

    public Reserializer(final String body) {
        this.body = body;
    }

    public MetadataUtils buildMetadataFromPPNApi(String ppn) {
        LOGGER.info("Getting Metadata from PPN API using {}", ppn);
        final Optional<MetsMods> metsMods = ofNullable(getMetadataFromAPIwithPPN(ppn));
        metsMods.ifPresentOrElse(mets -> {
            metadataUtils.setMetsMods(mets);
            metadataUtils.buildFinalMetadata();
        }, () -> {
            LOGGER.error("invalid PPN {}", ppn);
            throw new RuntimeException("Invalid PPN for manifest");
        });
        return metadataUtils;
    }

    public MetadataUtils buildMetadataFromURNApi(String urn) {

        LOGGER.info("Getting Metadata from URN API using {}", urn);
        final Optional<MetsMods> metsMods = ofNullable(getMetadataFromAPI(urn));
        metsMods.ifPresentOrElse(mets -> {
            metadataUtils.setMetsMods(mets);
            metadataUtils.buildFinalMetadata();
        }, () -> {
            LOGGER.error("invalid URN {}", urn);
            throw new RuntimeException("Invalid URN for manifest");
        });
        return metadataUtils;
    }

    public String build() {
        try {
            final Manifest manifest = MAPPER.readValue(body, new TypeReference<Manifest>() {
            });
            final Optional<List<Metadata>> metadata = ofNullable(manifest.getMetadata());
            if (metadata.isPresent()) {
                List<Metadata> harmonizedMetadata = harmonizeMetadataLabels(metadata.get());
                final Optional<Metadata> metaURN = harmonizedMetadata.stream().filter(
                        y -> y.getLabel().equals("URN")).findAny();
                final Optional<Metadata> metaPPN = harmonizedMetadata.stream().filter(
                        y -> y.getLabel().equals("Source PPN (SWB)")).findAny();
                if (metaURN.isPresent()) {
                    final String urn = metaURN.get().getValue();
                    if (urn.equals("null") && metaPPN.isPresent()) {
                        final String ppn = metaPPN.get().getValue();
                        metadataUtils = buildMetadataFromPPNApi(ppn);
                    } else {
                        metadataUtils = buildMetadataFromURNApi(urn);
                    }
                } else if (metaPPN.isPresent()) {
                    final String ppn = metaPPN.get().getValue();
                    metadataUtils = buildMetadataFromPPNApi(ppn);
                } else {
                    throw new RuntimeException("no valid identifiers for manifest");
                }
            }
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
                final AtomicInteger ai = new AtomicInteger(0);
                final Map<String, String> backReferenceMap = new HashMap<>();
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
                    final String structureId = s.getStructureId();
                    if (!structureId.contains("LOG") || !structureId.contains("r0")) {
                        if (ai.get() == 0) {
                            final String newStructureId = baseUrl + viewId + separator + structureBase + separator +
                                    "r0";
                            backReferenceMap.put(s.getStructureId(), newStructureId);
                            //unset within (fix for early manifests)
                            s.setWithin(null);
                            ai.getAndIncrement();
                        } else {
                            final String newStructureId = baseUrl + viewId + separator + structureBase + separator +
                                    "LOG_" + String.format(
                                    "%04d", ai.getAndIncrement());
                            backReferenceMap.put(s.getStructureId(), newStructureId);
                            //final Optional<List<String>> newRanges = ofNullable(rangeMap.get(newStructureId));
                            //newRanges.ifPresent(s::setRanges);
                            //unset within (fix for early manifests)
                            s.setWithin(null);
                        }
                    }
                });

                for (Structure struct : structs) {
                    final Optional<List<String>> fr = ofNullable(struct.getRanges());
                    final List<String> newRanges = new ArrayList<>();
                    if (fr.isPresent()) {
                        for (String r1 : fr.get()) {
                            final Optional<String> newRange = ofNullable(backReferenceMap.get(r1));
                            newRange.ifPresent(newRanges::add);
                        }
                        struct.setRanges(newRanges);
                    }
                    String structId = struct.getStructureId();
                    String newStructId = backReferenceMap.get(structId);
                    struct.setStructureId(newStructId);
                }
                perfectManifest.setStructures(structs);
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

    public List<String> getRelated(final String viewId, final String urn) {
        final ArrayList<String> related = new ArrayList<>();
        if (urn != null) {
            related.add(katalogUrl + urn);
        }
        related.add(viewerUrl + viewId);
        return related;
    }

    public List<URL> buildMetsModsJsonApiURLList() {
        final IRI jsonAPI = rdf.createIRI("http://localhost:8900/exist/restxq/mets");
        final String res;
        try {
            res = client.getDefaultType(jsonAPI);
            final RecordList recordList = MAPPER.readValue(res, new TypeReference<RecordList>() {
            });
            final List<URL> list = new ArrayList<>();
            recordList.getRecords().forEach(r -> {
                final String apiLink = jsonAPI.getIRIString() + separator + r.getUrn();
                try {
                    list.add(new URL(apiLink));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });
            return list;
        } catch (LdpClientException | IOException e) {
            throw new RuntimeException("Record List Api Request Failed");
        }
    }

    public MetsMods getMetadataFromAPI(String urn) {
        final IRI jsonAPI = rdf.createIRI("http://localhost:8900/exist/restxq/mets" + separator + urn);
        final String res;
        try {
            res = client.getDefaultType(jsonAPI);
            return MAPPER.readValue(res, new TypeReference<MetsMods>() {
            });
        } catch (LdpClientException | IOException e) {
            LOGGER.error("URN Api Request Failed for URN {}", urn);
            throw new RuntimeException("URN Api Request Failed");
        }
    }

    public MetsMods getMetadataFromAPIwithPPN(String ppn) {
        final IRI jsonAPI = rdf.createIRI("http://localhost:8900/exist/restxq/mets/ppn" + separator + ppn);
        final String res;
        try {
            res = client.getDefaultType(jsonAPI);
            return MAPPER.readValue(res, new TypeReference<MetsMods>() {
            });
        } catch (LdpClientException | IOException e) {
            LOGGER.error("PPN Api Request Failed for PPN {}", ppn);
            throw new RuntimeException("PPN Api Request Failed");
        }
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
