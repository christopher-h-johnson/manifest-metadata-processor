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

package de.ubleipzig.metadata.extractor.reserializer;

import static de.ubleipzig.metadata.processor.QueryUtils.readFile;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;
import de.ubleipzig.metadata.transformer.MetadataImplVersion2;
import de.ubleipzig.metadata.transformer.XmlDbAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class ManifestReserializerTest {
    private String testManifest = "http://iiif.ub.uni-leipzig.de/0000000054/manifest.json";
    private String testManifest1 = "http://iiif.ub.uni-leipzig.de/0000000018/manifest.json";
    private String xmldbhost = "http://workspaces.ub.uni-leipzig.de:8900";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testReserializeManifest() {
        try {
            final URL url = new URL(testManifest);
            final InputStream is = url.openStream();
            final String json = readFile(is);
            final Reserializer reserializer = new Reserializer(json, xmldbhost);
            System.out.println(reserializer.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testReserializeManifest2() {
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000009054/manifest.json");
            final InputStream is = url.openStream();
            final String json = readFile(is);
            final Reserializer reserializer = new Reserializer(json, xmldbhost);
            System.out.println(reserializer.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testReserializeManifest3() {
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000004595/manifest.json");
            final InputStream is = url.openStream();
            final String json = readFile(is);
            final ReserializerVersion3 reserializer = new ReserializerVersion3(json, xmldbhost);
            System.out.println(reserializer.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetUrlListFromAPI() {
        final XmlDbAccessor accessor = new XmlDbAccessor(xmldbhost);
        final List<URL> urlList = accessor.buildMetsModsJsonApiURLList();
        System.out.println(urlList);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetMetadataFromAPI() {
        final XmlDbAccessor accessor = new XmlDbAccessor(xmldbhost);
        final MetsMods metsmods = accessor.getMetadataFromAPI("urn:nbn:de:bsz:15-0011-224709");
        final Map<String, Object> metadata = metsmods.getMetadata();
        final List<Map<String, Object>> structures = metsmods.getStructures();
        final Optional<?> author = ofNullable(metadata.get("author"));
        final Optional<?> collection = ofNullable(metadata.get("collection"));
        final Optional<Map<String, String>> objAsMap = author.filter(Map.class::isInstance).map(Map.class::cast);
        final Optional<String> objAsString = collection.filter(String.class::isInstance).map(String.class::cast);
        final Optional<List<String>> objAsList = collection.filter(List.class::isInstance).map(List.class::cast);
        objAsList.ifPresent(System.out::println);
        objAsMap.ifPresent(System.out::println);
        objAsString.ifPresent(System.out::println);

    }

    @Test
    void testBuildAuthorsFromAPIList() {
        final MetadataImplVersion2 metadataUtils = new MetadataImplVersion2();
        final XmlDbAccessor accessor = new XmlDbAccessor(xmldbhost);
        final MetsMods metsmods = accessor.getMetadataFromAPI("urn:nbn:de:bsz:15-0012-142679");
        metadataUtils.setMetsMods(metsmods);
        final List<Metadata> authors = metadataUtils.setAuthors();
        System.out.println(authors.stream().map(Metadata::getValue)
                .collect( Collectors.joining( "," )));
    }

    @Test
    void testBuildAuthorFromAPIMap() {
        final MetadataImplVersion2 metadataUtils = new MetadataImplVersion2();
        final XmlDbAccessor accessor = new XmlDbAccessor(xmldbhost);
        final MetsMods metsmods = accessor.getMetadataFromAPI("urn:nbn:de:bsz:15-0012-220148");
        metadataUtils.setMetsMods(metsmods);
        final List<Metadata> authors = metadataUtils.setAuthors();
        System.out.println(authors.stream().map(Metadata::getValue)
                .collect( Collectors.joining( "," )));
    }
}

