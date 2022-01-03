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

import de.ubleipzig.metadata.producer.doc.MetsData;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.v2.Structure;
import de.ubleipzig.metadata.templates.v2.StructureList;
import de.ubleipzig.metadata.templates.v2.TopStructure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.ubleipzig.metadata.producer.doc.MetsConstants.METS_PARENT_LOGICAL_ID;
import static de.ubleipzig.metadata.producer.doc.MetsConstants.METS_STRUCTURE_TYPE;
import static de.ubleipzig.metadata.producer.doc.MetsManifestBuilder.*;
import static java.io.File.separator;
import static java.util.Collections.synchronizedList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class StructureBuilderVersion2 {
    private Map<String, List<MetsData.Xlink>> xlinkmap;
    private MetsData mets;
    private Config config;
    private String resourceContext;

    public StructureBuilderVersion2(final Config config, final MetsData mets, final String resourceContext) {
        this.config = config;
        this.mets = mets;
        this.xlinkmap = getXlinkMap();
        this.resourceContext = resourceContext;
    }

    public StructureList build() {
        TopStructure top = buildTopStructure();
        if (top.getRanges().size() > 0) {
            List<Structure> subStructures = buildStructures();
            return new StructureList(top, subStructures);
        }
        return null;
    }

    public TopStructure buildTopStructure() {
         final List<String> ranges = synchronizedList(new ArrayList<>());

        final List<MetsData.Logical> logs = getTopLogicals(mets);
        logs.forEach(logical -> {
            final String rangeId = resourceContext + config.getRangeContext() + separator + logical.getLogicalId();
            ranges.add(0, rangeId);
        });

        final TopStructure st = new TopStructure();
        st.setStructureId(resourceContext + config.getRangeContext() + separator + METS_PARENT_LOGICAL_ID);
        st.setStructureLabel("TOC");
        ranges.sort(naturalOrder());
        st.setRanges(ranges);
        return st;
    }

    public List<Metadata> buildStructureMetadata(final String logicalType) {
        final List<Metadata> metadataList = new ArrayList<>();
        final Metadata metadata = new Metadata();
        metadata.setLabel(METS_STRUCTURE_TYPE);
        metadata.setValue(logicalType);
        metadataList.add(metadata);
        return metadataList;
    }

    public List<Structure> buildStructures() {
        final List<Structure> structures = synchronizedList(new ArrayList<>());
        final List<Structure> descendents = synchronizedList(new ArrayList<>());
        xlinkmap.keySet().forEach(logical -> {
            final MetsData.Logical last = getLogicalLastDescendent(mets, logical);
            if (last != null) {
                final List<MetsData.Logical> logicalLastParentList = getLogicalLastParent(mets, last.getLogicalId());
                logicalLastParentList.forEach(logicalLastParent -> {
                    final String lastParentId = logicalLastParent.getLogicalId();
                    final List<MetsData.Logical> lastChildren = getLogicalLastChildren(mets, lastParentId);
                    final List<String> ranges = synchronizedList(new ArrayList<>());
                    lastChildren.forEach(desc -> {
                        final Structure descSt = new Structure();
                        final String descID = desc.getLogicalId().trim();
                        final String rangeId = resourceContext + config.getRangeContext() + separator + descID;
                        final String descLabel = getLogicalLabel(mets, descID);
                        final String logType = getLogicalType(mets, descID);
                        ranges.add(0, rangeId);
                        descSt.setStructureId(rangeId);
                        descSt.setStructureLabel(descLabel);
                        final List<Metadata> metadataList = buildStructureMetadata(logType);
                        descSt.setMetadata(metadataList);
                        descSt.setCanvases(getCanvases(descID));
                        descendents.add(0, descSt);
                    });
                    final Structure st = new Structure();
                    final String structureIdDesc = resourceContext + config.getRangeContext() + separator +
                            lastParentId;
                    st.setStructureId(structureIdDesc);
                    final String logicalLabel = getLogicalLabel(mets, lastParentId);
                    final String logType = getLogicalType(mets, lastParentId);
                    final List<Metadata> metadataList = buildStructureMetadata(logType);
                    st.setStructureLabel(logicalLabel);
                    st.setMetadata(metadataList);
                    ranges.sort(naturalOrder());
                    st.setRanges(ranges);
                    st.setCanvases(getCanvases(lastParentId));
                    if (!Objects.equals(
                            st.getStructureId(),
                            resourceContext + config.getRangeContext() + separator + METS_PARENT_LOGICAL_ID)) {
                        structures.add(0, st);
                    }
                });

            }
        });
        final Comparator<Structure> c = Comparator.comparing(Structure::getStructureId);
        return Stream.concat(structures.stream(), descendents.stream()).filter(
                new ConcurrentSkipListSet<>(c)::add).sorted(comparing(Structure::getStructureId)).collect(
                Collectors.toList());
    }

    public List<String> getCanvases(final String logical) {
        final IRIBuilder iri = new IRIBuilder(this.config);
        final List<String> canvases = new ArrayList<>();
        final List<String> physicals = xlinkmap.get(logical).stream().map(MetsData.Xlink::getXLinkTo).collect(toList());
        physicals.forEach(physical -> {
            canvases.add(iri.buildCanvasIRIfromPhysical(physical, resourceContext));
        });
        return canvases;
    }

    public Map<String, List<MetsData.Xlink>> getXlinkMap() {
        final List<MetsData.Xlink> xlinks = getXlinks(mets);
        return xlinks.stream().collect(groupingBy(MetsData.Xlink::getXLinkFrom));
    }
}
