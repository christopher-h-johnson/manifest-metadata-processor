package de.ubleipzig.metadata.extractor;

import static java.util.Optional.ofNullable;

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.Structure;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MetadataUtils {
    private MetsMods metsMods;
    private List<Metadata> finalMetadata = new ArrayList<>();

    public MetadataUtils(){}

    public List<Metadata> getFinalMetadata() {
        return finalMetadata;
    }

    public void setMetsMods(MetsMods metsMods) {this.metsMods = metsMods;}

    public List<Metadata> setAuthors() {
        Map<String, Object> newMetadata = metsMods.getMetadata();
        List<Metadata> mList = new ArrayList<>();
        if (getValueAsMap(newMetadata, "author").isPresent()) {
            Map<String, String> authorMap = getValueAsMap(newMetadata, "author").get();
            Optional<String> gnd = ofNullable(authorMap.get("GND"));
            if (gnd.isPresent()) {
                String author = authorMap.get("label");
                Metadata m1 = new Metadata();
                m1.setLabel("Author");
                m1.setValue(author + " <" + gnd.get() + ">");
                mList.add(m1);
            } else {
                Metadata m1 = new Metadata();
                m1.setLabel("Author");
                m1.setValue(authorMap.get("label"));
                mList.add(m1);
            }
        } else if (getValueAsMapList(newMetadata, "author").isPresent()) {
            List<Map<String, String>> authors = getValueAsMapList(newMetadata, "author").get();
            authors.forEach(m -> {
                Optional<String> gnd = ofNullable(m.get("GND"));
                if (gnd.isPresent()) {
                    String author = m.get("label");
                    Metadata m1 = new Metadata();
                    m1.setLabel("Author");
                    m1.setValue(author + " <" + gnd.get() + ">");
                    mList.add(m1);
                } else {
                    Metadata m1 = new Metadata();
                    m1.setLabel("Author");
                    m1.setValue(m.get("label"));
                    mList.add(m1);
                }
            });
        }
        return mList;
    }

    public List<Metadata> setCollections() {
        Map<String, Object> newMetadata = metsMods.getMetadata();
        List<Metadata> mList = new ArrayList<>();
        if (getValueAsString(newMetadata, "collection").isPresent()) {
            String collection = getValueAsString(newMetadata, "collection").get();
                Metadata m1 = new Metadata();
                m1.setLabel("Collection");
                m1.setValue(collection);
                mList.add(m1);
        } else if (getValueAsStringList(newMetadata, "collection").isPresent()) {
            List<String> collections = getValueAsStringList(newMetadata, "collection").get();
            collections.forEach(m -> {
                Metadata m1 = new Metadata();
                m1.setLabel("Collection");
                m1.setValue(m);
                mList.add(m1);
            });
        }
        return mList;
    }

    public List<Metadata> setLanguages() {
        Map<String, Object> newMetadata = metsMods.getMetadata();
        List<Metadata> mList = new ArrayList<>();
        if (getValueAsString(newMetadata, "language-iso639-2").isPresent()) {
            String collection = getValueAsString(newMetadata, "language-iso639-2").get();
            Metadata m1 = new Metadata();
            m1.setLabel("Language");
            m1.setValue(collection);
            mList.add(m1);
        } else if (getValueAsStringList(newMetadata, "language-iso639-2").isPresent()) {
            List<String> languages = getValueAsStringList(newMetadata, "language-iso639-2").get();
            languages.forEach(l -> {
                Metadata m1 = new Metadata();
                m1.setLabel("Language");
                m1.setValue(l);
                mList.add(m1);
            });
        }
        return mList;
    }

    public static Metadata setMetadataValue(Map<String, Object> newMetadata, String key, String displayLabel) {
        String value = getValueAsString(newMetadata, key).orElse(null);
        Metadata m1 = new Metadata();
        m1.setLabel(displayLabel);
        m1.setValue(value);
        return m1;
    }

    private static Optional<List<String>> getValueAsStringList(Map<String, Object> newMetadata, String key) {
        Optional<?> value = ofNullable(newMetadata.get(key));
        return value
                .filter(List.class::isInstance)
                .map(List.class::cast);
    }

    private static Optional<List<Map<String, String>>> getValueAsMapList(Map<String, Object> newMetadata, String key) {
        Optional<?> value = ofNullable(newMetadata.get(key));
        return value
                .filter(List.class::isInstance)
                .map(List.class::cast);
    }

    private static Optional<Map<String, String>> getValueAsMap(Map<String, Object> newMetadata, String key) {
        Optional<?> value = ofNullable(newMetadata.get(key));
        return value
                .filter(Map.class::isInstance)
                .map(Map.class::cast);
    }

    private static Optional<String> getValueAsString(Map<String, Object> newMetadata, String key) {
        Optional<?> value = ofNullable(newMetadata.get(key));
        return value
                .filter(String.class::isInstance)
                .map(String.class::cast);
    }

    public void buildFinalMetadata() {
        Map<String, Object> newMetadata = metsMods.getMetadata();
        Metadata v1 = setMetadataValue(newMetadata, "kitodo", "Kitodo");
        if(v1.getValue() != null) {finalMetadata.add(v1);}
        Metadata v2  = setMetadataValue(newMetadata, "urn", "URN");
        if(v2.getValue() != null) {finalMetadata.add(v2);}
        Metadata v3  = setMetadataValue(newMetadata, "ppn", "Source PPN (SWB)");
        if(v3.getValue() != null) {finalMetadata.add(v3);}
        Metadata v4  = setMetadataValue(newMetadata, "vd17", "VD17");
        if(v4.getValue() != null) {finalMetadata.add(v4);}
        Metadata v5  = setMetadataValue(newMetadata, "vd16", "VD16");
        if(v5.getValue() != null) {finalMetadata.add(v5);}
        Metadata v6 = setMetadataValue(newMetadata, "vd18", "VD18");
        if(v6.getValue() != null) {finalMetadata.add(v6);}
        Metadata v7 = setMetadataValue(newMetadata, "subTitle", "SubTitle");
        if(v7.getValue() != null) {finalMetadata.add(v7);}
        Metadata v8 = setMetadataValue(newMetadata, "manuscriptaMediaevalia", "Manuscripta Mediaevalia");
        if(v8.getValue() != null) {finalMetadata.add(v8);}
        Metadata v9 = setMetadataValue(newMetadata, "recordDate", "Record Date");
        if(v9.getValue() != null) {finalMetadata.add(v9);}
        Metadata v10 = setMetadataValue(newMetadata, "localisierung", "Lokalisierung");
        if(v10.getValue() != null) {finalMetadata.add(v10);}
        Metadata v11 = setMetadataValue(newMetadata, "date", "Date");
        if(v11.getValue() != null) {finalMetadata.add(v11);}
        Metadata v12 = setMetadataValue(newMetadata, "datierung", "Datierung");
        if(v12.getValue() != null) {finalMetadata.add(v12);}
        Metadata v13 = setMetadataValue(newMetadata, "publisher", "Publisher");
        if(v13.getValue() != null) {finalMetadata.add(v13);}
        Metadata v14 = setMetadataValue(newMetadata, "physicalDescription", "Physical Description");
        if(v14.getValue() != null) {finalMetadata.add(v14);}
        Metadata v15 = setMetadataValue(newMetadata, "umfang", "Umfang");
        if(v15.getValue() != null) {finalMetadata.add(v15);}
        Metadata v16 = setMetadataValue(newMetadata, "abmessung", "Abmessung");
        if(v16.getValue() != null) {finalMetadata.add(v16);}
        Metadata v17 = setMetadataValue(newMetadata, "medium", "Medium");
        if(v17.getValue() != null) {finalMetadata.add(v17);}
        Metadata v18 = setMetadataValue(newMetadata, "beschreibstoff", "Beschreibstoff");
        if(v18.getValue() != null) {finalMetadata.add(v18);}
        Metadata v19 = setMetadataValue(newMetadata, "manifestType", "Manifest Type");
        if(v19.getValue() != null) {finalMetadata.add(v19);}
        Metadata v20 = setMetadataValue(newMetadata, "callNumber", "Call Number");
        if(v20.getValue() != null) {finalMetadata.add(v20);}
        Metadata v21 = setMetadataValue(newMetadata, "script-iso15924", "Script Type");
        if(v21.getValue() != null) {finalMetadata.add(v21);}
        Metadata v22 = setMetadataValue(newMetadata, "place", "Place");
        if(v22.getValue() != null) {finalMetadata.add(v22);}
        List<Metadata> authors = setAuthors();
        List<Metadata> collections = setCollections();
        List<Metadata> languages = setLanguages();
        finalMetadata.addAll(collections);
        finalMetadata.addAll(authors);
        finalMetadata.addAll(languages);
        finalMetadata.sort(Comparator.comparing(Metadata::getLabel));
    }

    public List<Metadata> buildStructureMetadata() {
        List<Metadata> mList = new ArrayList<>();
        List<Map<String, Object>> structureMetadata = metsMods.getStructures();
        structureMetadata.forEach(sm -> {
            if (getValueAsMap(sm, "author").isPresent()) {
                Map<String, String> authorMap = getValueAsMap(sm, "author").get();
                Optional<String> gnd = ofNullable(authorMap.get("GND"));
                if (gnd.isPresent()) {
                    String author = authorMap.get("label");
                    Metadata m1 = new Metadata();
                    m1.setLabel("Author");
                    m1.setValue(author + " <" + gnd.get() + ">");
                    mList.add(m1);
                } else {
                    Metadata m1 = new Metadata();
                    m1.setLabel("Author");
                    m1.setValue(authorMap.get("label"));
                    mList.add(m1);
                }
            }
            Metadata v1 = setMetadataValue(sm, "structureType", "Structure Type");
            mList.add(v1);
        });
        return mList;
    }

    public static List<Metadata> harmonizeMetadataLabels(List<Metadata> metadata) {
        metadata.forEach(m -> {
            String label = m.getLabel();
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
