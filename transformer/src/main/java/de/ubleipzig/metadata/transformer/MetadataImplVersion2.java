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

package de.ubleipzig.metadata.transformer;

import de.ubleipzig.metadata.templates.ISO639;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;

import java.util.*;
import java.util.stream.Collectors;

import static de.ubleipzig.metadata.transformer.MetadataApiEnum.*;
import static java.util.Optional.ofNullable;

public class MetadataImplVersion2 extends MetadataObjectTypes implements MetadataApi<Metadata> {
    private static final ResourceBundle deutschLabels = ResourceBundle.getBundle("metadataLabels", Locale.GERMAN);
    private static final ResourceBundle englishLabels = ResourceBundle.getBundle("metadataLabels", Locale.ENGLISH);
    private static final String PERIOD = ".";
    private MetsMods metsMods;
    private List<Metadata> finalMetadata = new ArrayList<>();
    private final LanguageMap languageMap = new LanguageMap();

    public MetadataImplVersion2() {
    }

    @Override
    public List<Metadata> getMetadata() {
        return finalMetadata;
    }

    @Override
    public void setMetsMods(final MetsMods metsMods) {
        this.metsMods = metsMods;
    }

    private List<Metadata> setAuthor(List<Metadata> mList, final Map<String, String> authorMap) {
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

    private Metadata buildMetadata(final String label, final String value, final Integer displayOrder) {
        final Metadata metadata = new Metadata();
        metadata.setLabel(label);
        metadata.setValue(value);
        metadata.setDisplayOrder(displayOrder);
        return metadata;
    }

    public List<Metadata> setAuthors() {
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

    private String getGermanLanguageNameForCode(final String key) {
        final ISO639 iso639 = languageMap.getISO639();
        final List<ISO639.Language> languages = iso639.getLanguages();
        final Optional<ISO639.Language> lang = languages.stream().filter(y -> y.getIso639_2().equals(key)).findAny();
        return lang.map(ISO639.Language::getGermanName).orElse(null);
    }

    private List<Metadata> addMetadataStringOrList(final Map<String, Object> newMetadata, final String key,
                                                   final String displayLabel, Integer displayOrder) {
        final List<Metadata> mList = new ArrayList<>();
        if (getValueAsString(newMetadata, key).isPresent()) {
            final String value = getValueAsString(newMetadata, key).get();
            final Metadata m1 = buildMetadata(displayLabel, value, displayOrder);
            mList.add(m1);
            if (key.equals("language-iso639-2")) {
                //add German Language Name to Metadata
                final String germanLanguageName = getGermanLanguageNameForCode(value);
                final Metadata m2 = buildMetadata(LANGUAGE_NAME.getApiKey(), germanLanguageName, displayOrder + 1);
                mList.add(m2);
            }
        } else if (getValueAsStringList(newMetadata, key).isPresent()) {
            final List<String> values = getValueAsStringList(newMetadata, key).get();
            values.forEach(value -> {
                final Metadata m1 = buildMetadata(displayLabel, value, displayOrder);
                mList.add(m1);
                if (key.equals("language-iso639-2")) {
                    //add German Language Name to Metadata
                    final String germanLanguageName = getGermanLanguageNameForCode(value);
                    final Metadata m2 = buildMetadata(LANGUAGE_NAME.getApiKey(), germanLanguageName, displayOrder + 1);
                    mList.add(m2);
                }
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

    private List<Metadata> addMetadataObject(Map<String, Object> newMetadata, String key, String label,
                                             Integer displayOrder) {
        final Optional<String> value = getValueAsString(newMetadata, key);
        if (value.isPresent()) {
            final Metadata m = buildMetadata(label, value.get(), displayOrder);
            finalMetadata.add(m);
        }
        return finalMetadata;
    }

    @Override
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
                finalMetadata = addMetadataObject(newMetadata, SUBTITLE.getApiKey(), "Objekttitel", 2);
            } else {
                finalMetadata = addMetadataObject(newMetadata, SUBTITLE.getApiKey(), "Subtitle", 2);
                //only show Physical Dimension for Non-manuscripts
                finalMetadata = addMetadataObject(
                        newMetadata, PHYSICAL_DESCRIPTION.getApiKey(), "Physical Description", 10);
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

    @Override
    public List<Metadata> buildStructureMetadataForId(final String structId) {
        final List<Metadata> mList = new ArrayList<>();
        final String authorKey = AUTHOR.getApiKey();
        final String authorNameKey = LABEL.getApiKey();
        final String authorLabel = englishLabels.getString(authorKey);
        final String displayOrderKey = authorKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer authorLabelDisplayOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        final List<Map<String, Object>> structureMetadata = metsMods.getStructures();
        final Optional<List<Map<String, Object>>> filteredSubList = Optional.of(
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
}
