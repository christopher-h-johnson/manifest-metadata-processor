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

package de.ubleipzig.metadata.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import de.ubleipzig.metadata.templates.collections.SiteMap;
import de.ubleipzig.metadata.templates.indexer.ManifestUUIDs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
@Slf4j
public class SiteMapCreatorTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectMapper xmlMapper = new XmlMapper();

    @Test
    void testBuildSiteMaps() {
        final InputStream is = SiteMapCreatorTest.class.getResourceAsStream("/data/unified.json");
        final List<SiteMap.Location> urlList = new ArrayList<>();
        try {
            ManifestUUIDs map = MAPPER.readValue(is, new TypeReference<ManifestUUIDs>() {
            });
            Map<String, Object> manifestMap = map.getManifestMap();
            manifestMap.forEach((key, value) -> {
                final String url = "https://collections.iiif.cloud/view/" + key;
                SiteMap.Location loc = new SiteMap.Location();
                loc.setLoc(url);
                urlList.add(loc);
            });
            final SiteMap siteMap = new SiteMap();
            int chunkSize = 50000;
            List<List<SiteMap.Location>> urlLists = new ArrayList<>();
            for (int i = 0; i < urlList.size(); i += chunkSize) {
                int end = Math.min(urlList.size(), i + chunkSize);
                urlLists.add(urlList.subList(i, end));
            }
            AtomicInteger ai = new AtomicInteger(1);
            urlLists.forEach(l -> {
                siteMap.setUrl(l);
                xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
                final String fileName = "/tmp/sitemap" + ai.getAndIncrement() + ".xml";
                try {
                    xmlMapper.writeValue(new File(fileName), siteMap);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            });

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
