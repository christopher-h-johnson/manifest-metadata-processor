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
import de.ubleipzig.metadata.templates.BodleianMetadataMap;
import de.ubleipzig.metadata.templates.collections.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.rdf.api.IRI;
import org.apache.jena.commonsrdf.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Disabled
@Slf4j
public class Collection2LevelCollectorTest {

    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String baseUrl = "http://localhost:9098/extractor?type=extract&m=";

    @Test
    void buildCollectionsFromJson() {
        final IRI rootCollectionIRI = rdf.createIRI(
                "https://iiif.durham.ac.uk/manifests/trifle/collection/32150/t2cqj72p712g");
        try {
            final HttpResponse<?> res = client.getResponse(rootCollectionIRI);
            if (res.statusCode() == 200 | res.statusCode() == 301) {
                final String json = res.body().toString();
                final CollectionList collections = MAPPER.readValue(json, new TypeReference<CollectionList>() {
                });
                final List<ManifestItem> cList = collections.getCollections();
                final BodleianRootCollection rootCollection = new BodleianRootCollection();
                final List<de.ubleipzig.metadata.templates.collections.BodleianMapListCollection> mapListCollections
                        = new ArrayList<>();
                cList.forEach(c -> {
                    final String cid = c.getId();
                    final String label = c.getLabel();
                    final IRI cIRI = rdf.createIRI(cid);
                    try {
                        final HttpResponse<?> res1 = client.getResponse(cIRI);
                        if (res1.statusCode() == 200 | res1.statusCode() == 301) {
                            final String json1 = res1.body().toString();
                            final ManifestList subcollections = MAPPER.readValue(
                                    json1, new TypeReference<ManifestList>() {
                                    });
                            final Optional<List<ManifestItem>> manifestList = ofNullable(subcollections.getManifests());
                            List<BodleianMetadataMap> finalMapList = new ArrayList<>();
                            if (manifestList.isPresent()) {
                                for (ManifestItem m : manifestList.get()) {
                                    final IRI identifier = rdf.createIRI(m.getId());
                                    final IRI apiReq = rdf.createIRI(baseUrl + identifier.getIRIString());
                                    final HttpResponse<?> res3 = client.getResponse(apiReq);
                                    if (res3.statusCode() == 200 | res3.statusCode() == 301) {
                                        final String json3 = res3.body().toString();
                                        final Optional<BodleianMetadataMap> metadataMap = ofNullable(
                                                buildMetadataMap(json3));
                                        if (metadataMap.isPresent()) {
                                            final BodleianMetadataMap map = metadataMap.get();
                                            Map<Object, Object> metadata = map.getMetadataMap();
                                            metadata.put("collection", label);
                                            metadata.put("manifest", m.getId());
                                            map.setMetadataMap(metadata);
                                            finalMapList.add(map);
                                            log.info("adding {} from collection {} to indexable metadata",
                                                    identifier.getIRIString(), label);
                                        }
                                    }
                                }
                            }
                            final BodleianMapListCollection l = new BodleianMapListCollection();
                            l.setMapListCollection(finalMapList);
                            l.setId(c.getId());
                            l.setLabel(c.getLabel());
                            mapListCollections.add(l);
                        }
                    } catch (LdpClientException | IOException e) {
                        log.error(e.getMessage());
                    }
                });
                rootCollection.setRootCollection(mapListCollections);
                final String out = JsonSerializer.serialize(rootCollection).orElse("");
                JsonSerializer.writeToFile(out, new File("/tmp/beasely.json"));
            }
        } catch (LdpClientException | IOException e) {
            log.error(e.getMessage());
        }
    }

    public BodleianMetadataMap buildMetadataMap(final String json) {
        final BodleianMetadataMap metadataMap;
        try {
            metadataMap = MAPPER.readValue(json, new TypeReference<BodleianMetadataMap>() {
            });
            return metadataMap;
        } catch (IOException e) {
            log.info("unmappable metadata");
        }
        return null;
    }
}
