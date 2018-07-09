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

import static de.ubleipzig.metadata.extractor.EnglishManifestMetadataLabelsEnum.Author;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.AUTHOR;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.COLLECTION;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.GND;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.LABEL;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.LANGUAGE;
import static de.ubleipzig.metadata.extractor.MetadataApiEnum.STRUCTTYPE;
import static java.util.Optional.ofNullable;

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetadataUtils {
    private MetsMods metsMods;
    private List<Metadata> finalMetadata = new ArrayList<>();

    public MetadataUtils() {
    }

    public List<Metadata> getFinalMetadata() {
        return finalMetadata;
    }

    public void setMetsMods(MetsMods metsMods) {
        this.metsMods = metsMods;
    }

    public List<Metadata> setAuthors() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final List<Metadata> mList = new ArrayList<>();
        if (getValueAsMap(newMetadata, AUTHOR.getApiKey()).isPresent()) {
            final Map<String, String> authorMap = getValueAsMap(newMetadata, AUTHOR.getApiKey()).get();
            final Optional<String> gnd = ofNullable(authorMap.get(GND.getApiKey()));
            if (gnd.isPresent()) {
                final String author = authorMap.get(LABEL.getApiKey());
                final Metadata m1 = new Metadata();
                m1.setLabel(Author.getLabel());
                m1.setValue(author + " [" + gnd.get() + "]");
                mList.add(m1);
            } else {
                final Metadata m1 = new Metadata();
                m1.setLabel(Author.getLabel());
                m1.setValue(authorMap.get(LABEL.getApiKey()));
                mList.add(m1);
            }
        } else if (getValueAsMapList(newMetadata, AUTHOR.getApiKey()).isPresent()) {
            final List<Map<String, String>> authors = getValueAsMapList(newMetadata, AUTHOR.getApiKey()).get();
            authors.forEach(m -> {
                final Optional<String> gnd = ofNullable(m.get(GND.getApiKey()));
                if (gnd.isPresent()) {
                    final String author = m.get(LABEL.getApiKey());
                    final Metadata m1 = new Metadata();
                    m1.setLabel(Author.getLabel());
                    m1.setValue(author + " [" + gnd.get() + "]");
                    mList.add(m1);
                } else {
                    final Metadata m1 = new Metadata();
                    m1.setLabel(Author.getLabel());
                    m1.setValue(m.get(LABEL.getApiKey()));
                    mList.add(m1);
                }
            });
        }
        return mList;
    }

    public List<Metadata> setCollections() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final List<Metadata> mList = new ArrayList<>();
        if (getValueAsString(newMetadata, COLLECTION.getApiKey()).isPresent()) {
            final String collection = getValueAsString(newMetadata, COLLECTION.getApiKey()).get();
            final Metadata m1 = new Metadata();
            m1.setLabel("Collection");
            m1.setValue(collection);
            mList.add(m1);
        } else if (getValueAsStringList(newMetadata, COLLECTION.getApiKey()).isPresent()) {
            final List<String> collections = getValueAsStringList(newMetadata, COLLECTION.getApiKey()).get();
            collections.forEach(m -> {
                final Metadata m1 = new Metadata();
                m1.setLabel("Collection");
                m1.setValue(m);
                mList.add(m1);
            });
        }
        return mList;
    }

    public List<Metadata> setLanguages() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final List<Metadata> mList = new ArrayList<>();
        if (getValueAsString(newMetadata, LANGUAGE.getApiKey()).isPresent()) {
            final String language = getValueAsString(newMetadata, LANGUAGE.getApiKey()).get();
            final Metadata m1 = new Metadata();
            m1.setLabel("Language");
            m1.setValue(language);
            mList.add(m1);
        } else if (getValueAsStringList(newMetadata, LANGUAGE.getApiKey()).isPresent()) {
            final List<String> languages = getValueAsStringList(newMetadata, LANGUAGE.getApiKey()).get();
            languages.forEach(l -> {
                final Metadata m1 = new Metadata();
                m1.setLabel("Language");
                m1.setValue(l);
                mList.add(m1);
            });
        }
        return mList;
    }

    public static Metadata setMetadataValue(final Map<String, Object> newMetadata, final String key, final String
            displayLabel) {
        final String value = getValueAsString(newMetadata, key).orElse(null);
        final Metadata m1 = new Metadata();
        m1.setLabel(displayLabel);
        m1.setValue(value);
        return m1;
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

    public void buildFinalMetadata() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final Metadata v19 = setMetadataValue(newMetadata, "manifestType", "Manifest Type");
        if (v19.getValue() != null && v19.getValue().equals("manuscript")) {
            finalMetadata.add(v19);
            //hack for UBL mets/mods classification confusion
            final Metadata v7 = setMetadataValue(newMetadata, "subTitle", "Objekttitel");
            if (v7.getValue() != null) {
                finalMetadata.add(v7);
            }
        } else {
            finalMetadata.add(v19);
            final Metadata v7 = setMetadataValue(newMetadata, "subTitle", "Subtitle");
            if (v7.getValue() != null) {
                finalMetadata.add(v7);
            }
        }
        final Metadata v24 = setMetadataValue(newMetadata, "partType", "Part Type");
        if (v24.getValue() != null) {
            finalMetadata.add(v24);
        }
        final Metadata v25 = setMetadataValue(newMetadata, "partOrder", "Part Order");
        if (v25.getValue() != null) {
            finalMetadata.add(v25);
        }

        final List<Metadata> authors = setAuthors();
        final List<Metadata> collections = setCollections();
        final List<Metadata> languages = setLanguages();
        finalMetadata.addAll(authors);
        finalMetadata.addAll(collections);
        finalMetadata.addAll(languages);
        final Metadata v21 = setMetadataValue(newMetadata, "script-iso15924", "Script Type");
        if (v21.getValue() != null) {
            finalMetadata.add(v21);
        }
        final Metadata v11 = setMetadataValue(newMetadata, "date", "Date");
        if (v11.getValue() != null) {
            finalMetadata.add(v11);
        }
        final Metadata v12 = setMetadataValue(newMetadata, "datierung", "Datierung");
        if (v12.getValue() != null) {
            finalMetadata.add(v12);
        }
        final Metadata v10 = setMetadataValue(newMetadata, "localisierung", "Lokalisierung");
        if (v10.getValue() != null) {
            finalMetadata.add(v10);
        }
        final Metadata v22 = setMetadataValue(newMetadata, "place", "Place");
        if (v22.getValue() != null) {
            finalMetadata.add(v22);
        }
        final Metadata v13 = setMetadataValue(newMetadata, "publisher", "Publisher");
        if (v13.getValue() != null) {
            finalMetadata.add(v13);
        }

        //only show Physical Dimension for Non-manuscripts
        if (v19.getValue() != null && !v19.getValue().equals("manuscript")) {
            final Metadata v14 = setMetadataValue(newMetadata, "physicalDescription", "Physical Description");
            if (v14.getValue() != null) {
                finalMetadata.add(v14);
            }
        }

        final Metadata v15 = setMetadataValue(newMetadata, "umfang", "Umfang");
        if (v15.getValue() != null) {
            finalMetadata.add(v15);
        }
        final Metadata v16 = setMetadataValue(newMetadata, "abmessung", "Abmessung");
        if (v16.getValue() != null) {
            finalMetadata.add(v16);
        }
        final Metadata v17 = setMetadataValue(newMetadata, "medium", "Medium");
        if (v17.getValue() != null) {
            finalMetadata.add(v17);
        }
        final Metadata v18 = setMetadataValue(newMetadata, "beschreibstoff", "Beschreibstoff");
        if (v18.getValue() != null) {
            finalMetadata.add(v18);
        }
        final Metadata v20 = setMetadataValue(newMetadata, "callNumber", "Call Number");
        if (v20.getValue() != null) {
            finalMetadata.add(v20);
        }
        final Metadata v8 = setMetadataValue(newMetadata, "manuscriptaMediaevalia", "Manuscripta Mediaevalia");
        if (v8.getValue() != null) {
            finalMetadata.add(v8);
        }
        final Metadata v9 = setMetadataValue(newMetadata, "recordDate", "Record Date");
        if (v9.getValue() != null) {
            finalMetadata.add(v9);
        }
        final Metadata v2 = setMetadataValue(newMetadata, "urn", "URN");
        if (v2.getValue() != null) {
            finalMetadata.add(v2);
        }
        final Metadata v1 = setMetadataValue(newMetadata, "kitodo", "Kitodo");
        if (v1.getValue() != null) {
            finalMetadata.add(v1);
        }
        final Metadata v3 = setMetadataValue(newMetadata, "ppn", "Source PPN (SWB)");
        if (v3.getValue() != null) {
            finalMetadata.add(v3);
        }
        final Metadata v4 = setMetadataValue(newMetadata, "vd17", "VD17");
        if (v4.getValue() != null) {
            finalMetadata.add(v4);
        }
        final Metadata v5 = setMetadataValue(newMetadata, "vd16", "VD16");
        if (v5.getValue() != null) {
            finalMetadata.add(v5);
        }
        final Metadata v6 = setMetadataValue(newMetadata, "vd18", "VD18");
        if (v6.getValue() != null) {
            finalMetadata.add(v6);
        }
        //finalMetadata.sort(Comparator.comparing(Metadata::getLabel));
    }

    public List<Metadata> buildStructureMetadataForId(final String structId) {
        final List<Metadata> mList = new ArrayList<>();
        final List<Map<String, Object>> structureMetadata = metsMods.getStructures();
        final Optional<List<Map<String, Object>>> filteredSubList = ofNullable(
                structureMetadata.stream().filter(s -> s.containsValue(structId)).collect(Collectors.toList()));
        filteredSubList.ifPresent(maps -> maps.forEach(sm -> {
            if (getValueAsMap(sm, AUTHOR.getApiKey()).isPresent()) {
                final Map<String, String> authorMap = getValueAsMap(sm, AUTHOR.getApiKey()).get();
                final Optional<String> gnd = ofNullable(authorMap.get(GND.getApiKey()));
                if (gnd.isPresent()) {
                    final String author = authorMap.get(LABEL.getApiKey());
                    final Metadata m1 = new Metadata();
                    m1.setLabel(Author.getLabel());
                    m1.setValue(author + " [" + gnd.get() + "]");
                    mList.add(m1);
                } else {
                    final Metadata m1 = new Metadata();
                    m1.setLabel(Author.getLabel());
                    m1.setValue(authorMap.get(LABEL.getApiKey()));
                    mList.add(m1);
                }
            }
            final Metadata v1 = setMetadataValue(sm, STRUCTTYPE.getApiKey(), "Structure Type");
            if (v1.getValue() != null) {
                mList.add(v1);
            }
        }));
        if (!mList.isEmpty()) {
            return mList;
        } else {
            return null;
        }
    }

    public static List<Metadata> harmonizeMetadataLabels(final List<Metadata> metadata) {
        metadata.forEach(m -> {
            final String label = m.getLabel();
            switch (label) {
                case "urn":
                    m.setLabel("URN");
                    break;
                case "swb-ppn":
                    m.setLabel("Source PPN (SWB)");
                    break;
                case "goobi":
                    m.setLabel("Kitodo");
                    break;
                case "Callnumber":
                    m.setLabel("Call number");
                    break;
                case "Date":
                    m.setLabel("Date of publication");
                    break;
                case "datiert":
                    m.setLabel("Date of publication");
                    break;
                case "vd17":
                    m.setLabel("VD17");
                    break;
                case "Place":
                    m.setLabel("Place of publication");
                    break;
                case "Physical State":
                    m.setLabel("Physical description");
                    break;
                default:
                    break;
            }
        });
        return metadata;
    }
}
