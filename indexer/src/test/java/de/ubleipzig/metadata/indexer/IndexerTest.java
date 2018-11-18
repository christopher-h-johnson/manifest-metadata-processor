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

import static java.io.File.separator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.MapList;
import de.ubleipzig.metadata.templates.MapListCollection;
import de.ubleipzig.metadata.templates.MetadataMap;
import de.ubleipzig.metadata.templates.OrpAtom;
import de.ubleipzig.metadata.templates.OrpAtomList;
import de.ubleipzig.metadata.templates.collections.BodleianCollectionMapListIdentifier;
import de.ubleipzig.metadata.templates.collections.BodleianMapListCollection;
import de.ubleipzig.metadata.templates.collections.CollectionMapListIdentifier;
import de.ubleipzig.metadata.templates.collections.LandingDoc;
import de.ubleipzig.metadata.templates.collections.MDZIdentifiers;
import de.ubleipzig.metadata.templates.collections.ManifestUUIDMap;
import de.ubleipzig.metadata.templates.indexer.ElasticCreate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
public class IndexerTest {

    private static Logger logger = LoggerFactory.getLogger(IndexerTest.class);
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private final String contentTypeJson = "application/json";
    private final String elasticSearchHost = "http://workspaces.ub.uni-leipzig.de:9100/";
    private final String elasticSearchLocalhost = "http://localhost:9100/";
    private final String lineSeparator = "line.separator";
    private final String docTypeIndex = "_doc";
    private final String bulkContext = "_bulk";
    private final String manifestFileName = "/manifest.json";
    private final String tenDigitString = "%010d";
    private final String extractorBase = "http://localhost:9098/extractor?type=extract&m=";

    private static String getDocumentId() {
        return UUID.randomUUID().toString();
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<IRI> buildIRIList() {
        final String extractorService = "http://localhost:9098/extractor?type=extract&m=https://api" + ".digitale" +
                "-sammlungen.de/iiif/presentation/v2/bsb";

        final int loops = 120000;
        final List<IRI> list = new ArrayList<>();
        for (int i = 0; i < loops; i++) {
            final String pid = String.format(tenDigitString, i);
            final IRI identifier = rdf.createIRI(extractorService + pid + "/manifest");
            list.add(identifier);
        }
        return list;
    }

    @Test
    public void putElasticDoc() throws LdpClientException {
        final String indexName = "a1/_doc/7";
        final LandingDoc doc = new LandingDoc();
        doc.setTag1("Harvard Art Museums");
        doc.setTag2("/collection/harvard");
        doc.setTag4("5000");
        doc.setImageServiceIRI("https://ids.lib.harvard.edu/ids/iiif/400954326");
        final String out = JsonSerializer.serialize(doc).orElse("");
        InputStream target = new ByteArrayInputStream(out.getBytes());
        client.put(rdf.createIRI(elasticSearchHost + indexName), target, contentTypeJson);
    }

    @Test
    public void testGetJsonAPI() {
        //final List<IRI> list = buildIRIList();
        final InputStream jsonList = IndexerTest.class.getResourceAsStream("/data/mdz/ids3/MDZIdentifiers-10280000.json");

        try {
            MDZIdentifiers list = MAPPER.readValue(jsonList, new TypeReference<MDZIdentifiers>() {
            });
            final List<String> mdzIds = list.getIdentifiers();
            final List<MetadataMap> mapList = new ArrayList<>();
            mdzIds.forEach(i -> {
                try {
                    final IRI iri = rdf.createIRI(extractorBase + i);
                    final HttpResponse res = client.getResponse(iri);
                    if (res.statusCode() == 200) {
                        final String json = res.body().toString();
                        final MetadataMap metadataMap = MAPPER.readValue(json, new TypeReference<MetadataMap>() {
                        });
                        if (metadataMap.getMetadataMap().size() > 0) {
                            mapList.add(metadataMap);
                            logger.info("adding {} to indexable metadata", iri.getIRIString());
                        }
                    }
                } catch (LdpClientException | IOException e) {
                    e.printStackTrace();
                }
            });
            final MapList l = new MapList();
            l.setMapList(mapList);
            final String out = JsonSerializer.serialize(l).orElse("");
            JsonSerializer.writeToFile(out, new File("/tmp/mdz-metadata-10280000.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    void putJsonElastic() {
        final String baseUrl = "http://workspaces.ub.uni-leipzig.de:9100";
        final String indexName = "/m";
        final String indexType = "/iiif";
        final Indexer indexer = new Indexer();
        try {
            final String index = "{}";
            final InputStream is = new ByteArrayInputStream(index.getBytes());
            client.put(rdf.createIRI(baseUrl + indexName), is, contentTypeJson);
            final InputStream jsonList = IndexerTest.class.getResourceAsStream("/data/ubl-metadata.json");
            final MapList mapList = MAPPER.readValue(jsonList, new TypeReference<MapList>() {
            });
            final List<MetadataMap> m = mapList.getMapList();
            m.forEach(map -> {
                String json = JsonSerializer.serialize(map).orElse("");
                indexer.indexJson(baseUrl, getDocumentId(), indexName, indexType, json);
            });
            client.put(rdf.createIRI(baseUrl + indexName), is, contentTypeJson);
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonElasticBulk() throws LdpClientException {
        final Indexer indexer = new Indexer();
        final String indexName = "vp5";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName, IndexerTest.class.getResourceAsStream("/vp5-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            final InputStream jsonList = IndexerTest.class.getResourceAsStream("/data/vp-metadata.json");
            final OrpAtomList mapList = MAPPER.readValue(jsonList, new TypeReference<OrpAtomList>() {
            });
            final List<OrpAtom> m = mapList.getOrpAtomList();
            m.forEach(map -> {
                ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                sb.append(System.getProperty(lineSeparator));
                sb.append(JsonSerializer.serializeRaw(map).orElse(""));
                sb.append(System.getProperty(lineSeparator));
            });
            System.out.println(sb.toString());
            final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonCollectionElasticBulk() throws LdpClientException {
        final Indexer indexer = new Indexer();

        final String indexName = "gt3";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName,
                IndexerTest.class.getResourceAsStream("/ubl-dynamic-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            final InputStream jsonList = IndexerTest.class.getResourceAsStream("/data/getty-metadata.json");
            final CollectionMapListIdentifier mapList = MAPPER.readValue(
                    jsonList, new TypeReference<CollectionMapListIdentifier>() {
                    });
            final List<MapListCollection> m = mapList.getRootCollection();
            m.forEach(map -> {
                map.getMapListCollection().forEach(ml -> {
                    ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                    sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                    sb.append(JsonSerializer.serializeRaw(ml.getMetadataMap()).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                });
            });
            final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putBodleianCollectionElasticBulk() throws LdpClientException {
        final Indexer indexer = new Indexer();

        final String indexName = "dh1";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName,
                IndexerTest.class.getResourceAsStream("/ubl-dynamic-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            final InputStream jsonList = IndexerTest.class.getResourceAsStream(
                    "/data/durham/sudan-archive/beasely.json");
            final BodleianCollectionMapListIdentifier mapList = MAPPER.readValue(
                    jsonList, new TypeReference<BodleianCollectionMapListIdentifier>() {
                    });
            final List<BodleianMapListCollection> m = mapList.getRootCollection();
            m.forEach(map -> {
                map.getMapListCollection().forEach(ml -> {
                    ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                    sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                    sb.append(JsonSerializer.serializeRaw(ml.getMetadataMap()).orElse(""));
                    sb.append(System.getProperty(lineSeparator));
                });
            });
            final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonCollectionElasticBulk3() throws LdpClientException {
        final Indexer indexer = new Indexer();
        final String indexName = "mdz1";
        final String baseUrl = elasticSearchHost;
        final String bulkUri = baseUrl + bulkContext;
        indexer.createIndexMapping(baseUrl + indexName, IndexerTest.class.getResourceAsStream("/ubl-mapping.json"));
        final StringBuffer sb = new StringBuffer();
        try {
            InputStream jsonList = IndexerTest.class.getResourceAsStream("/data/mdz/mdz-metadata-10280000.json");
            final MapList mapList = MAPPER.readValue(jsonList, new TypeReference<MapList>() {
            });
            final List<MetadataMap> m = mapList.getMapList();
            m.forEach(map -> {
                ElasticCreate c = indexer.createDocument(indexName, docTypeIndex, getDocumentId());
                sb.append(JsonSerializer.serializeRaw(c).orElse(""));
                sb.append(System.getProperty(lineSeparator));
                sb.append(JsonSerializer.serializeRaw(map.getMetadataMap()).orElse(""));
                sb.append(System.getProperty(lineSeparator));
            });
            System.out.println(sb.toString());
            final InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.post(rdf.createIRI(bulkUri), is, contentTypeJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void putJsonAtomsElasticBulk4() {
        final Indexer indexer = new Indexer();
        final String indexName = "t6";
        final InputStream mapping = IndexerTest.class.getResourceAsStream("/ubl-dynamic-mapping.json");
        indexer.createIndexMapping(elasticSearchHost + indexName, mapping);
        final List<IRI> list = buildDisassemblerIRIList();
        list.forEach(iri -> {
            indexer.putJsonAtomsElasticBulk(iri, indexName);
        });
    }

    @Test
    void reserializeAllManifestsv2() {
        final List<IRI> list = buildReserializerIRIList();
        list.forEach(iri -> {
            try {
                final String iriString = iri.getIRIString();
                final String viewId = new URL(iriString).getQuery().split(separator)[3];
                //final String filePath = IndexerTest.class.getResource("/data").getPath() + separator + viewId + "
                // .json";
                final String filePath = "/tmp/manifests" + separator + viewId + ".json";
                final HttpResponse res = client.getResponse(iri);
                if (res.statusCode() == 200) {
                    final String json = res.body().toString();
                    JsonSerializer.writeToFile(json, new File(filePath));
                    logger.info("Writing View Id {} manifest to file {}", viewId, filePath);
                } else {
                    logger.warn("Reserializing View Id {} failed with statusCode {}", viewId, res.statusCode());
                }
            } catch (LdpClientException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void putContentAtomsElastic() {
        final int index = 9000;
        final String viewId = String.format(tenDigitString, index);
        final Indexer indexer = new Indexer();
        final String indexName = "t8";
        final InputStream mapping = IndexerTest.class.getResourceAsStream("/ubl-nested-atom-mapping.json");
        indexer.createIndexMapping(elasticSearchHost + indexName, mapping);
        final IRI iri = rdf.createIRI(
                "http://localhost:9098/extractor?type=disassemble&m=http://iiif.ub.uni-leipzig.de/" + viewId +
                        manifestFileName);
        indexer.putModernContentAtoms(iri, indexName);
    }


    private List<IRI> buildDisassemblerIRIList() {
        final String disassemblerService = "http://localhost:9098/extractor?type=disassemble&m=http://iiif.ub" +
                ".uni-leipzig.de/";
        final int loops = 13000;
        final List<IRI> list = new ArrayList<>();
        for (int i = 12; i < loops; i++) {
            final String pid = String.format(tenDigitString, i);
            final IRI identifier = rdf.createIRI(disassemblerService + pid + manifestFileName);
            list.add(identifier);
        }
        return list;
    }

    @Test
    void buildUUIDList() {
        final String manifestBase = "https://iiif.ub.uni-leipzig.de/";
        final int loops = 12152;
        final Map<String, Map<String, String>> manifestMap = new HashMap<>();
        for (int i = 12; i < loops; i++) {
            final String pid = String.format(tenDigitString, i);
            final String manifest = manifestBase + pid + manifestFileName;
            Map<String, String> manifestKV = new HashMap<>();
            final UUID uuid = UUIDv5.nameUUIDFromNamespaceAndString(UUIDv5.NAMESPACE_URL, manifest);
            manifestKV.put("manifest", manifest);
            manifestMap.put(uuid.toString(), manifestKV);
        }
        final ManifestUUIDMap map = new ManifestUUIDMap();
        map.setManifestMap(manifestMap);
        String json = JsonSerializer.serialize(map).orElse("");
        JsonSerializer.writeToFile(json, new File("/tmp/ubl-uuidMap.json"));
    }

    private List<IRI> buildReserializerIRIList() {
        final String disassemblerService =
                "http://localhost:9098/extractor?type=reserialize&version=2&m=http://iiif" + ".ub.uni-leipzig.de/";
        final int loops = 5000;
        final List<IRI> list = new ArrayList<>();
        for (int i = 12; i < loops; i++) {
            final String pid = String.format(tenDigitString, i);
            final IRI identifier = rdf.createIRI(disassemblerService + pid + manifestFileName);
            list.add(identifier);
        }
        return list;
    }
}
