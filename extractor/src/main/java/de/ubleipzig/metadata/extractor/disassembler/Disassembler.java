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

package de.ubleipzig.metadata.extractor.disassembler;

import static de.ubleipzig.metadata.extractor.ExtractorUtils.IIPSRV_DEFAULT;
import static de.ubleipzig.metadata.extractor.ExtractorUtils.getKeysByValue;
import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.atomic.AnnotationBodyAtom;
import de.ubleipzig.metadata.templates.atomic.AtomList;
import de.ubleipzig.metadata.templates.v2.Structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Disassembler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Disassembler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String body;

    public Disassembler(final String body) {
        this.body = body;
    }

    public String build() {
        try {
            final Manifest manifest = MAPPER.readValue(body, new TypeReference<Manifest>() {
            });
            final Optional<List<Metadata>> metadata = ofNullable(manifest.getMetadata());
            final Map<String, String> metadataMap = new HashMap<>();

            //set title in metadata
            String title = manifest.getLabel();
            metadataMap.put("Title", title);
            metadata.ifPresent(md -> md.forEach(m -> {
                metadataMap.put(m.getLabel(), m.getValue());
            }));

            //build structures objects
            final Optional<List<Structure>> structures = ofNullable(manifest.getStructures());
            final Map<String, List<String>> structureMap = new HashMap<>();
            final Map<String, String> structureLabelMap = new HashMap<>();
            structures.ifPresent(st -> st.forEach(s -> {
                structureLabelMap.put(s.getStructureId(), s.getStructureLabel());
                Optional<List<String>> canvases = ofNullable(s.getCanvases());
                canvases.ifPresent(strings -> {
                    structureMap.put(s.getStructureId(), strings);
                });
            }));

            final AtomicInteger ai = new AtomicInteger(1);
            final List<AnnotationBodyAtom> abaList = new ArrayList<>();

            manifest.getSequences().forEach(sq -> {
                sq.getCanvases().forEach(c -> {
                    final AnnotationBodyAtom aba = new AnnotationBodyAtom();
                    final Integer imageIndex = ai.getAndIncrement();
                    final Optional<Set<String>> structureSet = ofNullable(getKeysByValue(structureMap, c.getId()));
                    final Map<Integer, Structure> sMap = new HashMap<>();
                    final AtomicInteger ai2 = new AtomicInteger(1);
                    structureSet.ifPresent(structs -> structs.forEach(ss -> {
                        final Structure structure = new Structure();
                        structure.setStructureLabel(structureLabelMap.get(ss));
                        structure.setStructureId(ss);
                        sMap.put(ai2.getAndIncrement(), structure);
                    }));
                    aba.setStructureMap(sMap);
                    c.getImages().forEach(i -> {
                        String iiifService = i.getResource().getService().getId();
                        //hack to fix service
                        if (iiifService.contains(IIPSRV_DEFAULT)) {
                            iiifService = iiifService.replace(IIPSRV_DEFAULT, "iiif");
                        }
                        aba.setIiifService(iiifService);
                        aba.setImageIndex(imageIndex);
                        aba.setMetadata(metadataMap);
                        abaList.add(aba);
                    });
                });
            });
            final AtomList atomList = new AtomList();
            atomList.setAtomList(abaList);
            final Optional<String> json = serialize(atomList);
            return json.orElse(null);
        } catch (IOException ex) {
            throw new RuntimeException("Could not Disassemble Manifest", ex.getCause());
        }
    }
}
