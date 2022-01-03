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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.atomic.AnnotationBodyAtom;
import de.ubleipzig.metadata.templates.atomic.AtomList;
import de.ubleipzig.metadata.templates.v2.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.ubleipzig.metadata.extractor.ExtractorUtils.IIPSRV_DEFAULT;
import static de.ubleipzig.metadata.extractor.ExtractorUtils.getKeysByValue;
import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static java.util.Optional.ofNullable;

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
            final Map<String, Object> metadataMap = new HashMap<>();

            //set title in metadata
            String title = manifest.getLabel();
            metadataMap.put("Title", title);

            // set manifest Id in metadata
            String manifestId = manifest.getId();
            metadataMap.put("manifest", manifestId);

            // set license in metadata
            Optional<String> license = ofNullable(manifest.getLicense());
            license.ifPresent(s -> metadataMap.put("license", s));

            // set attribution in metadata
            String attribution = manifest.getAttribution();
            metadataMap.put("attribution", attribution);

            metadata.ifPresent(md -> md.forEach(m -> {
                final Optional<?> label = ofNullable(m.getLabel());
                final Optional<String> l = label.filter(String.class::isInstance).map(String.class::cast);
                final Optional<?> value = ofNullable(m.getValue());
                final Optional<String> v = value.filter(String.class::isInstance).map(String.class::cast);
                if (l.isPresent() && v.isPresent()) {
                    metadataMap.put(l.get(), v.get());
                }
            }));

            //build structures objects
            final Optional<List<Structure>> structures = ofNullable(manifest.getStructures());
            final Map<String, List<String>> structureMap = new HashMap<>();
            final Map<String, String> structureLabelMap = new HashMap<>();
            structures.ifPresent(st -> st.forEach(s -> {
                structureLabelMap.put(s.getStructureId(), (String) s.getStructureLabel());
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
                    c.getImages().forEach(i -> {
                        String thumbnail = i.getResource().getService().getId();
                        //hack to fix service
                        if (thumbnail.contains(IIPSRV_DEFAULT)) {
                            thumbnail = thumbnail.replace(IIPSRV_DEFAULT, "iiif");
                        }
                        if (!thumbnail.contains("https")) {
                            thumbnail = thumbnail.replace("http", "https");
                        }
                        Map<String, Object> mapCopy = metadataMap.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        mapCopy.put("thumbnail", thumbnail);
                        mapCopy.put("imageIndex", String.valueOf(imageIndex));
                        if (sMap.size() > 0) {
                            mapCopy.put("structureMap", sMap);
                        }
                        aba.setMetadata(mapCopy);
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
