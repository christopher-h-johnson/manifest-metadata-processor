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

import de.ubleipzig.metadata.templates.v2.Structure;
import de.ubleipzig.metadata.templates.v3.Item;
import de.ubleipzig.metadata.templates.v3.MetadataVersion3;
import de.ubleipzig.metadata.transformer.MetadataApi;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.baseUrl;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.structureBase;
import static de.ubleipzig.metadata.extractor.reserializer.ReserializerUtils.buildLabelMap;
import static de.ubleipzig.metadata.extractor.reserializer.ReserializerUtils.buildPaddedCanvases;
import static java.io.File.separator;
import static java.io.File.separatorChar;
import static java.util.Optional.ofNullable;

@Slf4j
public class StructureBuilderVersion3 {
    private final List<Structure> structures;
    private final String viewId;
    private final MetadataApi<MetadataVersion3> metadataImplVersion3;
    private final Map<String, String> backReferenceMap = new HashMap<>();

    public StructureBuilderVersion3(final List<Structure> structures, final String viewId,
                                    final MetadataApi<MetadataVersion3> metadataImplVersion3) {
        this.structures = structures;
        this.viewId = viewId;
        this.metadataImplVersion3 = metadataImplVersion3;
    }

    public void fix() {
        final AtomicInteger ai = new AtomicInteger(0);
        structures.forEach(s -> {
            final Optional<List<String>> cs = ofNullable(s.getCanvases());

            if (cs.isPresent()) {
                final List<String> paddedCanvases = buildPaddedCanvases(cs.get(), viewId);
                if (!paddedCanvases.isEmpty()) {
                    s.setCanvases(paddedCanvases);
                }
            }

            final String structureId = s.getId();
            if (!structureId.contains("LOG") || !structureId.contains("r0")) {
                if (ai.get() == 0) {
                    final String newStructureId = baseUrl + viewId + separator + structureBase + separator + "LOG_0000";
                    backReferenceMap.put(s.getId(), newStructureId);
                    //unset within (fix for early manifests)
                    s.setWithin(null);
                    ai.getAndIncrement();
                } else {
                    final String newStructureId =
                            baseUrl + viewId + separator + structureBase + separator + "LOG_" + String.format(
                            "%04d", ai.getAndIncrement());
                    backReferenceMap.put(s.getId(), newStructureId);
                    //unset within (fix for early manifests)
                    s.setWithin(null);
                }
            }
        });
    }

    public List<Item> build() {
        final List<Item> newStructures = new ArrayList<>();
        for (Structure struct : structures) {
            final Item newStructure = new Item();
            final Object structureLabel = struct.getLabel();
            final Map<String, List<String>> labelMap = buildLabelMap((String) structureLabel, "de");
            newStructure.setLabel(labelMap);
            final Optional<List<String>> fr = ofNullable(struct.getRanges());
            final List<Item> newRanges = new ArrayList<>();
            final List<Item> newCanvases = new ArrayList<>();
            final Optional<List<String>> canvases = ofNullable(struct.getCanvases());
            canvases.ifPresent(cs -> cs.forEach(c -> {
                final Item newCanvas = new Item();
                newCanvas.setId(c);
                newCanvas.setType("Canvas");
                newCanvases.add(newCanvas);
            }));

            if (fr.isPresent()) {
                for (String r1 : fr.get()) {
                    final Optional<String> newRange = ofNullable(backReferenceMap.get(r1));
                    newRange.ifPresent(r -> {
                        final Item nr = new Item();
                        nr.setId(r);
                        nr.setType("Range");
                        newRanges.add(nr);
                    });
                }
            }
            final Stream<Item> combinedItems = Stream.concat(newRanges.stream(), newCanvases.stream());
            final List<Item> newItems = combinedItems.collect(Collectors.toList());
            newStructure.setItems(newItems);
            final String structId = struct.getId();
            final String newStructId = backReferenceMap.get(structId);
            newStructure.setId(newStructId);
            final String sId;
            try {
                sId = new URI(newStructId).toURL().getPath().split(String.valueOf(separatorChar))[3];
                //TODO
                final Optional<List<MetadataVersion3>> structureMetadata = ofNullable(
                        metadataImplVersion3.buildStructureMetadataForId(sId));
                //structureMetadata.ifPresent(struct::setMetadata);
            } catch (MalformedURLException e) {
                log.error(e.getMessage());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            newStructures.add(newStructure);
        }
        //finally merge all substructures into top structure
        final Optional<Item> topStructure = newStructures.stream().filter(
                s -> s.getId().contains("LOG_0000")).findAny();
        if (topStructure.isPresent()) {
            final List<Item> topStructureItems = topStructure.get().getItems();
            topStructureItems.forEach(ti -> {
                final Optional<Item> item = newStructures.stream().filter(
                        s -> s.getId().contains(ti.getId())).findAny();
                if (item.isPresent()) {
                    final Map<String, List<String>> label = item.get().getLabel();
                    ti.setLabel(label);
                    final List<Item> items = item.get().getItems();
                    final List<Item> ranges = items.stream().filter(t -> t.getType().equals("Range")).collect(
                            Collectors.toList());
                    if (ranges.isEmpty()) {
                        ti.setItems(items);
                    } else {
                        final List<Item> subItems = new ArrayList<>();
                        ranges.forEach(si -> {
                            final Item subItem = new Item();
                            final String siId = si.getId();
                            final Map<String, List<String>> labelMap = si.getLabel();
                            subItem.setLabel(labelMap);
                            subItem.setId(siId);
                            final List<Item> subRange = newStructures.stream().filter(
                                    i -> i.getId().equals(siId)).collect(Collectors.toList());
                            final List<Item> subRangeCanvases = subRange.get(0).getItems();
                            subItem.setItems(subRangeCanvases);
                            subItems.add(subItem);
                        });
                        ti.setItems(subItems);
                    }
                }
            });
            final List<Item> finalStructure = new ArrayList<>();
            final Item top = topStructure.get();
            top.setType("Range");
            finalStructure.add(top);
            return finalStructure;
        }
        return null;
    }
}
