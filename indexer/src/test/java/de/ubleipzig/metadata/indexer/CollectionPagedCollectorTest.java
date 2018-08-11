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
import de.ubleipzig.metadata.templates.collections.PagedCollection;
import de.ubleipzig.metadata.templates.collections.RootCollection;
import de.ubleipzig.metadata.templates.collections.TopCollection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jdk.incubator.http.HttpResponse;

import org.apache.commons.collections4.CollectionUtils;
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
public class CollectionPagedCollectorTest {

    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String baseUrl = "http://localhost:9098/extractor?type=extract&m=";
    private final Indexer indexer = new Indexer();
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionPagedCollectorTest.class);

    private List<MetadataMap> buildMetadataMapList(final List<ManifestItem> manifestList) {
        final List<MetadataMap> metadataMapList = new ArrayList<>();
        for (ManifestItem m : manifestList) {
            final IRI identifier = rdf.createIRI(m.getId());
            final IRI apiReq = rdf.createIRI(baseUrl + identifier.getIRIString());
            final HttpResponse res3;
            try {
                res3 = client.getResponse(apiReq);
                if (res3.statusCode() == 200 | res3.statusCode() == 301) {
                    final String json3 = res3.body().toString();
                    final MetadataMap metadataMap = indexer.buildMetadataMap(json3);
                    Map<String, String> metadata = metadataMap.getMetadataMap();
                    metadata.put("manifest", m.getId());
                    metadataMap.setMetadataMap(metadata);
                    metadataMapList.add(metadataMap);
                    LOGGER.info("adding {} to indexable metadata", identifier.getIRIString());
                }
            } catch (LdpClientException e) {
                e.printStackTrace();
            }
        }
        return metadataMapList;
    }

    private Iterable<ManifestItem> concatManifestListAsIterable(List<ManifestItem> listA, List<ManifestItem> listB) {
        return CollectionUtils.union(listA, listB);
    }

    private String getJsonResponse(final IRI cIRI) {
        final HttpResponse res1;
        try {
            res1 = client.getResponse(cIRI);
            if (res1.statusCode() == 200 | res1.statusCode() == 301) {
                return res1.body().toString();
            }
        } catch (LdpClientException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("request failed");
    }

    @Test
    void buildCollectionsFromJson() throws IOException {
        final IRI rootCollectionIRI = rdf.createIRI("https://iiif.harvardartmuseums.org/collections/top");
        final String json = getJsonResponse(rootCollectionIRI);
        final TopCollection collections = MAPPER.readValue(json, new TypeReference<TopCollection>() {
        });
        final List<PagedCollection> cList = collections.getMembers();
        final RootCollection rootCollection = new RootCollection();
        final List<MapListCollection> mapListCollections = new ArrayList<>();
        for (PagedCollection c : cList) {
            final String collectionId = c.getId();
            final IRI cIRI0 = rdf.createIRI(collectionId);
            final String json0 = getJsonResponse(cIRI0);
            final PagedCollection subTop = MAPPER.readValue(json0, new TypeReference<PagedCollection>() {
            });
            final Integer total = subTop.getTotal();
            //final int loops = total / 100;
            final int loops = 10;
            String nextId = null;
            List<ManifestItem> manifestList = null;
            Iterable<ManifestItem> items = null;
            List<ManifestItem> itemList = null;
            for (int i = 0; i < loops; i++) {
                String firstId;
                if (i == 0) {
                    firstId = subTop.getFirst();
                    final IRI cIRI = rdf.createIRI(firstId);
                    final String json1 = getJsonResponse(cIRI);
                    final PagedCollection subCollections = MAPPER.readValue(
                            json1, new TypeReference<PagedCollection>() {
                            });
                    manifestList = subCollections.getManifests();
                    nextId = subCollections.getNext();
                } else if (i == 1) {
                    final IRI cIRI1 = rdf.createIRI(nextId);
                    final String json2 = getJsonResponse(cIRI1);
                    final PagedCollection subCollectionsNext = MAPPER.readValue(
                            json2, new TypeReference<PagedCollection>() {
                            });
                    List<ManifestItem> subC = subCollectionsNext.getManifests();
                    items = concatManifestListAsIterable(manifestList, subC);
                    nextId = subCollectionsNext.getNext();
                } else {
                    final IRI cIRI12 = rdf.createIRI(nextId);
                    final String json3 = getJsonResponse(cIRI12);
                    final PagedCollection subCollectionsNext = MAPPER.readValue(
                            json3, new TypeReference<PagedCollection>() {
                            });
                    final List<ManifestItem> subC = subCollectionsNext.getManifests();
                    if (items != null) {
                        itemList = StreamSupport.stream(items.spliterator(), false).collect(Collectors.toList());
                    }
                    items = concatManifestListAsIterable(itemList, subC);
                    nextId = subCollectionsNext.getNext();
                }
            }
            if (items != null) {
                List<ManifestItem> finalManifestList = StreamSupport.stream(items.spliterator(), false).collect(
                        Collectors.toList());
                final List<MetadataMap> finalMapList = buildMetadataMapList(finalManifestList);
                final MapListCollection l = new MapListCollection();
                l.setMapListCollection(finalMapList);
                l.setId(c.getId());
                mapListCollections.add(l);
                rootCollection.setRootCollection(mapListCollections);
                final String out = JsonSerializer.serialize(rootCollection).orElse("");
                JsonSerializer.writeToFile(out, new File("/tmp/harvardArt-metadata.json"));
            }
        }
    }
}