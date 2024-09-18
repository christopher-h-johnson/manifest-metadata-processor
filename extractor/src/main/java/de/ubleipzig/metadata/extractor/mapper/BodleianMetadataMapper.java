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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ubleipzig.metadata.templates.BodleianMetadataMap;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.v2.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static java.util.Optional.ofNullable;

@Slf4j
public class BodleianMetadataMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String body;

    public BodleianMetadataMapper(final String body) {
        this.body = body;
    }

    private String getRandomImageAsThumbnail(final PerfectManifest manifest) {
        final List<Sequence> seq = manifest.getSequences();
        final List<Canvas> canvases = seq.get(0).getCanvases();
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


    public String build() {
        try {
            final PerfectManifest manifest = MAPPER.readValue(body, new TypeReference<PerfectManifest>() {
            });

            final Map<Object, Object> metadataMap = new HashMap<>();

            //get Manifest Id
            final Optional<String> id = ofNullable(manifest.getId());
            id.ifPresent(t -> metadataMap.put("manifest", id.get()));

            //get Thumbnail
            //final Optional<Object> thumbnail = ofNullable(manifest.getThumbnail());
            final Optional<String> thumb = ofNullable(getRandomImageAsThumbnail(manifest));
            thumb.ifPresent(t -> metadataMap.put("thumbnail", thumb.get()));

            final String title = manifest.getLabel();
            metadataMap.put("title", title);

            final Optional<List<Metadata>> metadata = ofNullable(manifest.getMetadata());
            if (metadata.isPresent()) {
                final List<String> subjects = new ArrayList<>();
                final List<String> types = new ArrayList<>();
                final List<String> descriptions = new ArrayList<>();
                final List<String> formats = new ArrayList<>();
                final List<String> contributors = new ArrayList<>();
                final List<String> identifiers = new ArrayList<>();
                final List<String> titles = new ArrayList<>();
                final List<String> coverages = new ArrayList<>();
                final List<String> sources = new ArrayList<>();
                final List<String> incipits = new ArrayList<>();
                final List<String> languages = new ArrayList<>();
                final List<String> displayLanguages = new ArrayList<>();
                final List<String> collections = new ArrayList<>();
                final List<String> dates = new ArrayList<>();
                final List<String> alternatives = new ArrayList<>();
                final List<String> creators = new ArrayList<>();
                final List<String> locations = new ArrayList<>();
                for (Metadata m : metadata.get()) {
                    if (m.getLabel().equals("Subject")) {
                        final Optional<String> subject = ofNullable((String) m.getValue());
                        subject.ifPresent(subjects::add);
                    }
                    if (m.getLabel().equals("Type")) {
                        final Optional<String>  type = ofNullable((String) m.getValue());
                        type.ifPresent(types::add);
                    }
                    if (m.getLabel().equals("Description")) {
                        final Optional<String>  description = ofNullable((String) m.getValue());
                        description.ifPresent(descriptions::add);
                    }
                    if (m.getLabel().equals("Format")) {
                        final Optional<String>  format = ofNullable((String) m.getValue());
                        format.ifPresent(formats::add);
                    }
                    if (m.getLabel().equals("Contributor")) {
                        final Optional<String>  contributor = ofNullable((String) m.getValue());
                        contributor.ifPresent(contributors::add);
                    }
                    if (m.getLabel().equals("Identifier")) {
                        final Optional<String>  identifier = ofNullable((String) m.getValue());
                        identifier.ifPresent(identifiers::add);
                    }
                    if (m.getLabel().equals("Title")) {
                        final Optional<String>  ti = ofNullable((String) m.getValue());
                        ti.ifPresent(titles::add);
                    }
                    if (m.getLabel().equals("Coverage")) {
                        final Optional<String>  coverage = ofNullable((String) m.getValue());
                        coverage.ifPresent(coverages::add);
                    }
                    if (m.getLabel().equals("Source")) {
                        final Optional<String>  source = ofNullable((String) m.getValue());
                        source.ifPresent(sources::add);
                    }
                    if (m.getLabel().equals("Incipit")) {
                        final Optional<String>  incipit = ofNullable((String) m.getValue());
                        incipit.ifPresent(incipits::add);
                    }
                    if (m.getLabel().equals("Language")) {
                        final Optional<String>  language = ofNullable((String) m.getValue());
                        language.ifPresent(languages::add);
                    }
                    if (m.getLabel().equals("Displaylanguage")) {
                        final Optional<String>  displayLanguage = ofNullable((String) m.getValue());
                        displayLanguage.ifPresent(displayLanguages::add);
                    }
                    if (m.getLabel().equals("Collection")) {
                        final Optional<String>  collection = ofNullable((String) m.getValue());
                        collection.ifPresent(collections::add);
                    }
                    if (m.getLabel().equals("Date")) {
                        final Optional<String>  date = ofNullable((String) m.getValue());
                        date.ifPresent(dates::add);
                    }
                    if (m.getLabel().equals("Alternative")) {
                        final Optional<String>  alternative = ofNullable((String) m.getValue());
                        alternative.ifPresent(alternatives::add);
                    }
                    if (m.getLabel().equals("Creator")) {
                        final Optional<String>  creator = ofNullable((String) m.getValue());
                        creator.ifPresent(creators::add);
                    }
                    if (m.getLabel().equals("Location")) {
                        final Optional<String>  location = ofNullable((String) m.getValue());
                        location.ifPresent(locations::add);
                    }
                    if (m.getLabel().equals("Accessrights")) {
                        final Optional<String>  access = ofNullable((String) m.getValue());
                        metadataMap.put("accessRights", access.get());
                    }
                    if (m.getLabel().equals("Publisher")) {
                        final Optional<String>  publisher = ofNullable((String) m.getValue());
                        metadataMap.put("publisher", publisher.get());
                    }
                    if (m.getLabel().equals("Shelfmark")) {
                        final Optional<String>  shelfmark = ofNullable((String) m.getValue());
                        metadataMap.put("shelfmark", shelfmark.get());
                    }
                    if (m.getLabel().equals("Catalogueid")) {
                        final Optional<String>  catalogueId = ofNullable((String) m.getValue());
                        metadataMap.put("catalogueId", catalogueId.get());
                    }
                }
                if (!subjects.isEmpty()) {
                    metadataMap.put("subjects", subjects);
                }
                if (!types.isEmpty()) {
                    metadataMap.put("types", types);
                }
                if (!descriptions.isEmpty()) {
                    metadataMap.put("descriptions", descriptions);
                }
                if (!formats.isEmpty()) {
                    metadataMap.put("formats", formats);
                }
                if (!contributors.isEmpty()) {
                    metadataMap.put("contributors", contributors);
                }
                if (!identifiers.isEmpty()) {
                    metadataMap.put("identifiers", identifiers);
                }
                if (!titles.isEmpty()) {
                    metadataMap.put("titles", titles);
                }
                if (!coverages.isEmpty()) {
                    metadataMap.put("coverages", coverages);
                }
                if (!sources.isEmpty()) {
                    metadataMap.put("sources", sources);
                }
                if (!incipits.isEmpty()) {
                    metadataMap.put("incipits", incipits);
                }
                if (!languages.isEmpty()) {
                    metadataMap.put("languages", languages);
                }
                if (!displayLanguages.isEmpty()) {
                    metadataMap.put("displayLanguages", displayLanguages);
                }
                if (!collections.isEmpty()) {
                    metadataMap.put("collections", collections);
                }
                if (!dates.isEmpty()) {
                    metadataMap.put("dates", dates);
                }
                if (!alternatives.isEmpty()) {
                    metadataMap.put("alternatives", alternatives);
                }
                if (!creators.isEmpty()) {
                    metadataMap.put("creators", creators);
                }
                if (!locations.isEmpty()) {
                    metadataMap.put("locations", locations);
                }
            }
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
                    final Optional<List<String>> vl = seeAlso.filter(List.class::isInstance).map(List.class::cast);
                    if (vl.isPresent()) {
                        List<String> s = vl.get();
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
                final Optional<String> lisc = license.map(String.class::cast);
                lisc.ifPresent(s -> metadataMap.put("license", s));
            }

            //set attribution  (only if string)
            final Optional<?> attribution = ofNullable(manifest.getAttribution());
            if (attribution.isPresent()) {
                final Optional<String> attr = attribution.map(String.class::cast);
                attr.ifPresent(s -> metadataMap.put("attribution", s));
            }
            final BodleianMetadataMap map = new BodleianMetadataMap();
            map.setMetadataMap(metadataMap);
            final Optional<String> json = serialize(map);
            return json.orElse(null);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
