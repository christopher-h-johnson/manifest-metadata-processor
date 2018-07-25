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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.MetadataMap;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MetadataMapper {
    private String body;
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataMapper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public MetadataMapper(final String body) {
        this.body = body;
    }

    private String getRandomImageAsThumbnail(final PerfectManifest manifest) {
        final List<Sequence> seq = manifest.getSequences();
        final List<Canvas> canvases = seq.get(0).getCanvases();
        final Integer canvasCount = canvases.size();
        int n = new SplittableRandom().nextInt(0, canvasCount);
        final List<PaintingAnnotation> images = canvases.get(n).getImages();
        final Body res = images.get(0).getBody();
        return res.getService().getId();
    }

    public String build() {
        try {
            final PerfectManifest manifest = MAPPER.readValue(body, new TypeReference<PerfectManifest>() {
            });
            final Map<String, String> metadataMap = new HashMap<>();

            //get Thumbnail
            final Optional<Object> thumbnail = ofNullable(manifest.getThumbnail());
            final String thumb;
            thumb = thumbnail.map(t -> (String) t).orElseGet(() -> getRandomImageAsThumbnail(manifest));
            metadataMap.put("thumbnail", thumb);

            final Optional<List<Metadata>> metadata = ofNullable(manifest.getMetadata());
            final String title = manifest.getLabel();
            metadataMap.put("title", title);

            //set related (only if string)
            final Optional<?> related = ofNullable(manifest.getRelated());
            if (related.isPresent()) {
                final Optional<String> rel = related.filter(String.class::isInstance).map(String.class::cast);
                rel.ifPresent(s -> metadataMap.put("related", s));
            }

            metadata.ifPresent(md -> md.forEach(m -> {
                metadataMap.put(m.getLabel(), m.getValue());
            }));
            final MetadataMap map = new MetadataMap();
            map.setMetadataMap(metadataMap);
            final Optional<String> json = serialize(map);
            return json.orElse(null);
        } catch (IOException ex) {
            LOGGER.warn("Could not Map Manifest Metadata");
            throw new RuntimeException("Could not Map Manifest Metadata", ex.getCause());
        }
    }
}
