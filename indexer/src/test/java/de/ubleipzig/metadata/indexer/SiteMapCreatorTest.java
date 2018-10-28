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

import org.junit.jupiter.api.Test;

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
            Map<String,Object> manifestMap = map.getManifestMap();
            manifestMap.forEach((key, value) -> {
                final String url = "https://collections.iiif.cloud/view/" + key;
                SiteMap.Location loc = new SiteMap.Location();
                loc.setLoc(url);
                urlList.add(loc);
            });
            final SiteMap siteMap = new SiteMap();
            int chunkSize = 50000;
            List<List<SiteMap.Location>> urlLists = new ArrayList<>();
            for (int i=0; i<urlList.size(); i+= chunkSize) {
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
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
