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

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.v2.Structure;
import de.ubleipzig.metadata.transformer.MetadataApi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.baseUrl;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.structureBase;
import static de.ubleipzig.metadata.extractor.reserializer.ReserializerUtils.buildPaddedCanvases;
import static java.io.File.separator;
import static java.util.Optional.ofNullable;

public class StructureBuilder {
    private final List<Structure> structures;
    private final String viewId;
    private final MetadataApi<Metadata> metadataImplVersion2;
    private final Map<String, String> backReferenceMap = new HashMap<>();

    public StructureBuilder(final List<Structure> structures, final String viewId,
                            final MetadataApi<Metadata> metadataImplVersion2) {
        this.structures = structures;
        this.viewId = viewId;
        this.metadataImplVersion2 = metadataImplVersion2;
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

            final String structureId = s.getStructureId();
            if (!structureId.contains("LOG") || !structureId.contains("r0")) {
                if (ai.get() == 0) {
                    final String newStructureId = baseUrl + viewId + separator + structureBase + separator + "LOG_0000";
                    backReferenceMap.put(s.getStructureId(), newStructureId);
                    //unset within (fix for early manifests)
                    s.setWithin(null);
                    s.setViewingHint("top");
                    ai.getAndIncrement();
                } else {
                    final String newStructureId =
                            baseUrl + viewId + separator + structureBase + separator + "LOG_" + String.format(
                            "%04d", ai.getAndIncrement());
                    backReferenceMap.put(s.getStructureId(), newStructureId);
                    //unset within (fix for early manifests)
                    s.setWithin(null);
                }
            }
        });
    }

    public List<Structure> build() {
        for (Structure struct : structures) {
            final Optional<List<String>> fr = ofNullable(struct.getRanges());
            final List<String> newRanges = new ArrayList<>();
            if (fr.isPresent()) {
                for (String r1 : fr.get()) {
                    final Optional<String> newRange = ofNullable(backReferenceMap.get(r1));
                    newRange.ifPresent(newRanges::add);
                }
                struct.setRanges(newRanges);
            }
            final String structId = struct.getStructureId();
            final String newStructId = backReferenceMap.get(structId);
            struct.setStructureId(newStructId);
            final String sId;
            try {
                sId = new URL(newStructId).getPath().split(separator)[3];
                final Optional<List<Metadata>> structureMetadata = ofNullable(
                        metadataImplVersion2.buildStructureMetadataForId(sId));
                structureMetadata.ifPresent(struct::setMetadata);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return structures;
    }
}
