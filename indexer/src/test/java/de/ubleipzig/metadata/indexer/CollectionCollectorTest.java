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

import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.MapListCollection;
import de.ubleipzig.metadata.templates.MetadataMap;
import de.ubleipzig.metadata.templates.collections.ManifestItem;
import de.ubleipzig.metadata.templates.collections.ManifestList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jdk.incubator.http.HttpResponse;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

@Disabled
public class CollectionCollectorTest {

    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String baseUrl = "http://localhost:9098/extractor?type=extract&m=";
    private final Indexer indexer = new Indexer();
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionCollectorTest.class);

    @Test
    void buildCollectionsFromJson() {
        final IRI rootCollectionIRI = rdf.createIRI("https://biblissima.fr/iiif/collection/gallica-bnf/mss-clairvaux/");
        try {
            final HttpResponse res = client.getResponse(rootCollectionIRI);
            if (res.statusCode() == 200 | res.statusCode() == 301) {
                final String json1 = res.body().toString();
                final ManifestList subcollections = MAPPER.readValue(json1, new TypeReference<ManifestList>() {
                });
                final List<ManifestItem> manifestList = subcollections.getManifests();
                List<MetadataMap> finalMapList = new ArrayList<>();
                for (ManifestItem m : manifestList) {
                    final IRI identifier = rdf.createIRI(m.getId());
                    final IRI apiReq = rdf.createIRI(baseUrl + identifier.getIRIString());
                    final HttpResponse res3 = client.getResponse(apiReq);
                    if (res3.statusCode() == 200 | res3.statusCode() == 301) {
                        final String json3 = res3.body().toString();
                        final MetadataMap metadataMap = indexer.buildMetadataMap(json3);
                        Map<String, String> metadata = metadataMap.getMetadataMap();
                        metadata.put("manifest", m.getId());
                        metadataMap.setMetadataMap(metadata);
                        finalMapList.add(metadataMap);
                        LOGGER.info("adding {} to indexable metadata", identifier.getIRIString());
                    }
                }
                final MapListCollection l = new MapListCollection();
                l.setMapListCollection(finalMapList);
                final String out = JsonSerializer.serialize(l).orElse("");
                JsonSerializer.writeToFile(out, new File("/tmp/gallica-mss-clairvaux-metadata.json"));
            }
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }
}
