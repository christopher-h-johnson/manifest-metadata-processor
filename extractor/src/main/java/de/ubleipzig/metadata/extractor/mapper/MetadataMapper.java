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

package de.ubleipzig.metadata.extractor.mapper;

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.templates.BodleianMetadataMap;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.v2.Body;
import de.ubleipzig.metadata.templates.v2.Canvas;
import de.ubleipzig.metadata.templates.v2.PaintingAnnotation;
import de.ubleipzig.metadata.templates.v2.PerfectManifest;
import de.ubleipzig.metadata.templates.v2.Sequence;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MetadataMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataMapper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String body;

    public MetadataMapper(final String body) {
        this.body = body;
    }

    private String getRandomImageAsThumbnail(final PerfectManifest manifest) {
        final Optional<List<Sequence>> seq = ofNullable(manifest.getSequences());
        if (seq.isPresent()) {
            final List<Canvas> canvases = seq.get().get(0).getCanvases();
            final int canvasCount = canvases.size();
            if (canvasCount == 1) {
                final List<PaintingAnnotation> images = canvases.get(0).getImages();
                final Body res = images.get(0).getBody();
                return res.getService().getId();
            } else if (canvasCount > 1) {
                int n = new SplittableRandom().nextInt(0, canvasCount);
                final List<PaintingAnnotation> images = canvases.get(n).getImages();
                final Body res = images.get(0).getBody();
                return res.getService().getId();
            } else {
                return null;
            }
        }
        return null;
    }

    public String build() {
        try {
            final PerfectManifest manifest = MAPPER.readValue(body, new TypeReference<PerfectManifest>() {
            });

            final Map<String, Object> metadataMap = new HashMap<>();

            //get Manifest Id
            final Optional<String> id = ofNullable(manifest.getId());
            id.ifPresent(t -> metadataMap.put("manifest", id.get()));

            //get Thumbnail
            //final Optional<Object> thumbnail = ofNullable(manifest.getThumbnail());
            final Optional<String> thumb;
            thumb = ofNullable(getRandomImageAsThumbnail(manifest));
            thumb.ifPresent(t -> metadataMap.put("thumbnail", t));

            final String title = manifest.getLabel();
            metadataMap.put("title", title);

            final Optional<List<Metadata>> metadata = ofNullable(manifest.getMetadata());

            //set related (only if string)
            final Optional<?> related = ofNullable(manifest.getRelated());
            if (related.isPresent()) {
                final Optional<String> rel = related.filter(String.class::isInstance).map(String.class::cast);
                rel.ifPresent(s -> metadataMap.put("related", s));
            }

            //set seeAlso
            final Optional<?> seeAlso = ofNullable(manifest.getSeeAlso());
            if (seeAlso.isPresent()) {
                final Optional<String> see = seeAlso.filter(String.class::isInstance).map(String.class::cast);
                if (see.isPresent()) {
                    metadataMap.put("seeAlso", see.get());
                } else {
                    @SuppressWarnings("unchecked")
                    final Optional<List<Object>> vl = seeAlso.filter(List.class::isInstance).map(List.class::cast);
                    if (vl.isPresent()) {
                        List<Object> s = vl.get();
                        String vals = s.stream().map(Object::toString).collect(Collectors.joining(","));
                        metadataMap.put("seeAlso", vals);
                    }
                }
            }

            //set description  (only if string)
            final Optional<?> description = ofNullable(manifest.getDescription());
            if (description.isPresent()) {
                final Optional<String> desc = description.filter(String.class::isInstance).map(String.class::cast);
                desc.ifPresent(s -> metadataMap.put("description", s));
            }

            //set license  (only if string)
            final Optional<?> license = ofNullable(manifest.getLicense());
            if (license.isPresent()) {
                final Optional<String> lisc = license.filter(String.class::isInstance).map(String.class::cast);
                lisc.ifPresent(s -> metadataMap.put("license", s));
            }

            //set attribution  (only if string)
            final Optional<?> attribution = ofNullable(manifest.getAttribution());
            if (attribution.isPresent()) {
                final Optional<String> attr = attribution.filter(String.class::isInstance).map(String.class::cast);
                attr.ifPresent(s -> metadataMap.put("attribution", s));
            }

            metadata.ifPresent(md -> md.forEach(m -> {
                final Optional<?> value = ofNullable(m.getValue());
                final Optional<String> v = value.filter(String.class::isInstance).map(String.class::cast);
                if (v.isPresent()) {
                    metadataMap.put(m.getLabel(), v.get());
                } else {
                    @SuppressWarnings("unchecked")
                    final Optional<List<String>> vl = value.filter(List.class::isInstance).map(List.class::cast);
                    if (vl.isPresent()) {
                        List<String> s = vl.get();
                        //final String vals = s.stream().map(Object::toString).collect(Collectors.joining(","));
                        // String vals = s.stream().map(Object::toString).collect(Collectors.joining(","));
                        metadataMap.put(m.getLabel(), s);
                    }
                }
            }));
            final BodleianMetadataMap map = new BodleianMetadataMap();
            map.setMetadataMap(metadataMap);
            final Optional<String> json = serialize(map);
            return json.orElse(null);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
