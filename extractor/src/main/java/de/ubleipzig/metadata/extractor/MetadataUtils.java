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

import static de.ubleipzig.metadata.extractor.MetadataApiEnum.AUTHOR;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.COLLECTION;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.DISPLAYORDER;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.GND;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.LABEL;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.LANGUAGE;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.MANIFESTTYPE;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.MANUSCRIPT;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.STRUCTTYPE;
import static java.util.Optional.ofNullable;

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MetadataUtils {
    private MetsMods metsMods;
    private List<Metadata> finalMetadata = new ArrayList<>();
    private static final ResourceBundle deutschLabels = ResourceBundle.getBundle("metadataLabels", Locale.GERMAN);
    private static final ResourceBundle englishLabels = ResourceBundle.getBundle("metadataLabels", Locale.ENGLISH);
    private static final String PERIOD = ".";

    public MetadataUtils() {
    }

    public List<Metadata> getFinalMetadata() {
        return finalMetadata;
    }

    public void setMetsMods(MetsMods metsMods) {
        this.metsMods = metsMods;
    }

    private List<Metadata> setAuthor(List<Metadata> mList, Map<String, String> authorMap) {
        final Optional<String> gnd = ofNullable(authorMap.get(GND.getApiKey()));
        final String authorKey = AUTHOR.getApiKey();
        final String authorLabel = englishLabels.getString(authorKey);
        final String displayOrderKey = authorKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer authorLabelDisplayOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        if (gnd.isPresent()) {
            final String author = authorMap.get(LABEL.getApiKey());
            final String authorValue = author + " [" + gnd.get() + "]";
            final Metadata m1 = buildMetadata(authorLabel, authorValue, authorLabelDisplayOrder);
            mList.add(m1);
        } else {
            final String authorValue = authorMap.get(LABEL.getApiKey());
            final Metadata m1 = buildMetadata(authorLabel, authorValue, authorLabelDisplayOrder);
            mList.add(m1);
        }
        return mList;
    }

    private Metadata buildMetadata(String label, String value, Integer displayOrder) {
        Metadata metadata = new Metadata();
        metadata.setLabel(label);
        metadata.setValue(value);
        metadata.setDisplayOrder(displayOrder);
        return metadata;
    }

    List<Metadata> setAuthors() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final String authorKey = AUTHOR.getApiKey();
        List<Metadata> mList = new ArrayList<>();
        if (getValueAsMap(newMetadata, authorKey).isPresent()) {
            final Map<String, String> authorMap = getValueAsMap(newMetadata, authorKey).get();
            mList = setAuthor(mList, authorMap);
        } else if (getValueAsMapList(newMetadata, authorKey).isPresent()) {
            final List<Map<String, String>> authors = getValueAsMapList(newMetadata, authorKey).get();
            for (Map<String, String> authorMap : authors) {
                mList = setAuthor(mList, authorMap);
            }
        }
        return mList;
    }

    private List<Metadata> addMetadataStringOrList(final Map<String, Object> newMetadata, final String key, final
    String displayLabel, Integer displayOrder) {
        final List<Metadata> mList = new ArrayList<>();
        if (getValueAsString(newMetadata, key).isPresent()) {
            final String collection = getValueAsString(newMetadata, key).get();
            final Metadata m1 = buildMetadata(displayLabel, collection, displayOrder);
            mList.add(m1);
        } else if (getValueAsStringList(newMetadata, key).isPresent()) {
            final List<String> collections = getValueAsStringList(newMetadata, key).get();
            collections.forEach(collection -> {
                final Metadata m1 = buildMetadata(displayLabel, collection, displayOrder);
                mList.add(m1);
            });
        }
        return mList;
    }

    private List<Metadata> setCollections() {
        final String collectionKey = COLLECTION.getApiKey();
        final String collectionLabel = englishLabels.getString(collectionKey);
        final String displayOrderKey = collectionKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer collectionLabelOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        return addMetadataStringOrList(newMetadata, collectionKey, collectionLabel, collectionLabelOrder);
    }

    private List<Metadata> setLanguages() {
        final String languageKey = LANGUAGE.getApiKey();
        final String languageLabel = englishLabels.getString(languageKey);
        final String displayOrderKey = languageKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer languageLabelOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        return addMetadataStringOrList(newMetadata, languageKey, languageLabel, languageLabelOrder);
    }

    @SuppressWarnings("unchecked")
    private static Optional<List<String>> getValueAsStringList(final Map<String, Object> newMetadata, final String
            key) {
        final Optional<?> value = ofNullable(newMetadata.get(key));
        return value.filter(List.class::isInstance).map(List.class::cast);
    }

    @SuppressWarnings("unchecked")
    private static Optional<List<Map<String, String>>> getValueAsMapList(final Map<String, Object> newMetadata, final
    String key) {
        final Optional<?> value = ofNullable(newMetadata.get(key));
        return value.filter(List.class::isInstance).map(List.class::cast);
    }

    @SuppressWarnings("unchecked")
    private static Optional<Map<String, String>> getValueAsMap(final Map<String, Object> newMetadata, final String
            key) {
        final Optional<?> value = ofNullable(newMetadata.get(key));
        return value.filter(Map.class::isInstance).map(Map.class::cast);
    }

    private static Optional<String> getValueAsString(final Map<String, Object> newMetadata, final String key) {
        final Optional<?> value = ofNullable(newMetadata.get(key));
        return value.filter(String.class::isInstance).map(String.class::cast);
    }

    private List<Metadata> addMetadataObject(Map<String, Object> newMetadata, String key, String label, Integer
            displayOrder) {
        final Optional<String> value = getValueAsString(newMetadata, key);
        if (value.isPresent()) {
            final Metadata m = buildMetadata(label, value.get(), displayOrder);
            finalMetadata.add(m);
        }
        return finalMetadata;
    }

    public void buildFinalMetadata() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final List<Metadata> authors = setAuthors();
        final List<Metadata> collections = setCollections();
        final List<Metadata> languages = setLanguages();
        finalMetadata.addAll(authors);
        finalMetadata.addAll(collections);
        finalMetadata.addAll(languages);

        final String manifestTypeKey = MANIFESTTYPE.getApiKey();
        final Optional<String> manifestType = getValueAsString(newMetadata, manifestTypeKey);
        if (manifestType.isPresent()) {
            final String manifestTypeLabel = englishLabels.getString(manifestTypeKey);
            final String manifestTypeLabelDisplayOrderKey = manifestTypeKey + PERIOD + DISPLAYORDER.getApiKey();
            final Integer displayOrder = Integer.valueOf(englishLabels.getString(manifestTypeLabelDisplayOrderKey));
            final Metadata manifestTypeObj = buildMetadata(manifestTypeLabel, manifestType.get(), displayOrder);
            finalMetadata.add(manifestTypeObj);
            if (manifestType.get().equals(MANUSCRIPT.getApiKey())) {
                //hack for UBL mets/mods classification confusion
                finalMetadata = addMetadataObject(newMetadata, "subTitle", "Objekttitel", 2);
            } else {
                finalMetadata = addMetadataObject(newMetadata, "subTitle", "Subtitle", 2);
                //only show Physical Dimension for Non-manuscripts
                finalMetadata = addMetadataObject(newMetadata, "physicalDescription", "Physical Description", 10);
            }
        }

        final Set<String> enFilteredLabels = buildFilteredLabelSet(englishLabels);
        final Set<String> deFilteredLabels = buildFilteredLabelSet(deutschLabels);
        setFilteredLabelMetadata(newMetadata, enFilteredLabels, englishLabels);
        setFilteredLabelMetadata(newMetadata, deFilteredLabels, deutschLabels);
        finalMetadata.sort(Comparator.comparing(Metadata::getDisplayOrder));
    }

    private void setFilteredLabelMetadata(final Map<String, Object> newMetadata, final Set<String> filteredLabels,
                                          final ResourceBundle bundle) {
        filteredLabels.forEach(l -> {
            final String displayLabel = bundle.getString(l);
            final String displayOrderKey = l + PERIOD + DISPLAYORDER.getApiKey();
            final Integer displayOrder = Integer.valueOf(bundle.getString(displayOrderKey));
            finalMetadata = addMetadataObject(newMetadata, l, displayLabel, displayOrder);
        });
    }

    private Set<String> buildFilteredLabelSet(final ResourceBundle bundle) {
        final Enumeration<String> bundleLabelKeys = bundle.getKeys();
        final Stream<String> labelStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(bundleLabelKeys.asIterator(), Spliterator.ORDERED), false);
        return labelStream.filter(
                (s) -> !s.contains(DISPLAYORDER.getApiKey()) && !s.contains(LANGUAGE.getApiKey()) && !s.contains(
                        COLLECTION.getApiKey()) && !s.contains(AUTHOR.getApiKey()) && !s.contains(
                        MANIFESTTYPE.getApiKey()) && !s.contains(STRUCTTYPE.getApiKey())).collect(Collectors.toSet());
    }

    public List<Metadata> buildStructureMetadataForId(final String structId) {
        final List<Metadata> mList = new ArrayList<>();
        final String authorKey = AUTHOR.getApiKey();
        final String authorNameKey = LABEL.getApiKey();
        final String authorLabel = englishLabels.getString(authorKey);
        final String displayOrderKey = authorKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer authorLabelDisplayOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        final List<Map<String, Object>> structureMetadata = metsMods.getStructures();
        final Optional<List<Map<String, Object>>> filteredSubList = ofNullable(
                structureMetadata.stream().filter(s -> s.containsValue(structId)).collect(Collectors.toList()));
        filteredSubList.ifPresent(maps -> maps.forEach(sm -> {
            if (getValueAsMap(sm, authorKey).isPresent()) {
                final Map<String, String> authorMap = getValueAsMap(sm, authorKey).get();
                final Optional<String> gnd = ofNullable(authorMap.get(GND.getApiKey()));
                if (gnd.isPresent()) {
                    final String author = authorMap.get(authorNameKey);
                    final String authorValue = author + " [" + gnd.get() + "]";
                    final Metadata m1 = buildMetadata(authorLabel, authorValue, authorLabelDisplayOrder);
                    mList.add(m1);
                } else {
                    final String authorValue = authorMap.get(authorNameKey);
                    final Metadata m1 = buildMetadata(authorLabel, authorValue, authorLabelDisplayOrder);
                    mList.add(m1);
                }
            }
            final String structureTypeKey = STRUCTTYPE.getApiKey();
            final Optional<String> structureType = getValueAsString(sm, structureTypeKey);
            if (structureType.isPresent()) {
                final String structureTypeLabel = englishLabels.getString(structureTypeKey);
                final Metadata structureTypeObj = buildMetadata(structureTypeLabel, structureType.get(), 1);
                mList.add(structureTypeObj);
            }
        }));
        if (!mList.isEmpty()) {
            return mList;
        } else {
            return null;
        }
    }

    public static List<Metadata> harmonizeIdentifierLabels(final List<Metadata> metadata) {
        metadata.forEach(m -> {
            final String label = m.getLabel();
            switch (label) {
                case "urn":
                    m.setLabel(englishLabels.getString("urn"));
                    break;
                case "swb-ppn":
                    m.setLabel(englishLabels.getString("ppn"));
                    break;
                default:
                    break;
            }
        });
        return metadata;
    }
}
