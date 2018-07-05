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

import static de.ubleipzig.metadata.processor.QueryUtils.readFile;
import static java.util.Optional.ofNullable;

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ManifestReserializerTest {

    @Test
    void testReserializeManifest() {
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000000054/manifest.json");
            InputStream is = url.openStream();
            String json = readFile(is);
            final Reserializer reserializer = new Reserializer(json);
            System.out.println(reserializer.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testReserializeManifest2() {
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000009054/manifest.json");
            InputStream is = url.openStream();
            String json = readFile(is);
            final Reserializer reserializer = new Reserializer(json);
            System.out.println(reserializer.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetUrlListFromAPI() {
        try {
        final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000000018/manifest.json");
        InputStream is = url.openStream();
        String json = readFile(is);
        final Reserializer reserializer = new Reserializer(json);
        List<URL> urlList = reserializer.buildMetsModsJsonApiURLList();
        System.out.println(urlList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetMetadataFromAPI() {
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000000018/manifest.json");
            InputStream is = url.openStream();
            String json = readFile(is);
            final Reserializer reserializer = new Reserializer(json);
            MetsMods metsmods = reserializer.getMetadataFromAPI("urn:nbn:de:bsz:15-0011-224709");
            Map<String, Object> metadata = metsmods.getMetadata();
            List<Map<String, Object>> structures = metsmods.getStructures();
            Optional<?> author = ofNullable(metadata.get("author"));
            Optional<?> collection = ofNullable(metadata.get("collection"));
            Optional<Map<String, String>> objAsMap = author
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast);
            Optional<String> objAsString = collection
                    .filter(String.class::isInstance)
                    .map(String.class::cast);
            Optional<List<String>> objAsList = collection
                    .filter(List.class::isInstance)
                    .map(List.class::cast);
            objAsList.ifPresent(System.out::println);
            objAsMap.ifPresent(System.out::println);
            objAsString.ifPresent(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testBuildAuthorsFromAPIList() {
        final MetadataUtils metadataUtils = new MetadataUtils();
        try {
        final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000000018/manifest.json");
        InputStream is = url.openStream();
        String json = readFile(is);
        final Reserializer reserializer = new Reserializer(json);
            MetsMods metsmods = reserializer.getMetadataFromAPI("urn:nbn:de:bsz:15-0012-142679");
            metadataUtils.setMetsMods(metsmods);
        List<Metadata> authors = metadataUtils.setAuthors();
        System.out.println(authors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testBuildAuthorFromAPIMap() {
        final MetadataUtils metadataUtils = new MetadataUtils();
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000000018/manifest.json");
            InputStream is = url.openStream();
            String json = readFile(is);
            final Reserializer reserializer = new Reserializer(json);
            MetsMods metsmods = reserializer.getMetadataFromAPI("urn:nbn:de:bsz:15-0012-220148");
            metadataUtils.setMetsMods(metsmods);
            List<Metadata> authors = metadataUtils.setAuthors();
            System.out.println(authors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

