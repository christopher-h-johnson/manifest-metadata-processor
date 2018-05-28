package de.ubleipzig.metadata.processor;

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static de.ubleipzig.metadata.processor.JsonSerializer.writeToFile;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

import jdk.incubator.http.HttpResponse;

public class CollectionCollectorTest {
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Logger logger = getLogger(CollectionCollectorTest.class);
    private static final String baseUrl =
            "http://localhost:9098/extractor?type=extract&manifest=";

    @Test
    void buildCollectionsFromJson() {
        IRI rootCollectionIRI = rdf.createIRI("https://www.e-codices.unifr.ch/metadata/iiif/collection.json");
        try {
            HttpResponse res = client.getResponse(rootCollectionIRI);
            if (res.statusCode() == 200) {
                String json = res.body().toString();
                final CollectionList collections = MAPPER.readValue(json, new TypeReference<CollectionList>() {});
                List<Manifest> cList = collections.getCollections();
                RootCollection rootCollection = new RootCollection();
                List<MapListCollection> mapListCollections = new ArrayList<>();
                cList.subList(0, 2).forEach(c -> {
                    String cid = c.getId();
                    IRI cIRI = rdf.createIRI(cid);
                    try {
                        HttpResponse res1 = client.getResponse(cIRI);
                        if (res.statusCode() == 200) {
                            String json1 = res1.body().toString();
                            final ManifestList subcollections = MAPPER.readValue(
                                    json1, new TypeReference<ManifestList>() {});
                            List<Manifest> manifestList = subcollections.getManifests();
                            List<MetadataMap> mapList = new ArrayList<>();
                            manifestList.subList(0, 2).forEach(m -> {
                                final IRI identifier = rdf.createIRI(m.getId());

                                     try {
                                        IRI apiReq = rdf.createIRI(baseUrl + identifier.getIRIString());
                                        HttpResponse res3 = client.getResponse(apiReq);
                                        if (res3.statusCode() == 200) {
                                            String json3 = res3.body().toString();
                                            final MetadataMap metadataMap = MAPPER.readValue(
                                                    json3, new TypeReference<MetadataMap>() {});
                                            if (metadataMap.getMetadataMap().size() > 0) {
                                                mapList.add(metadataMap);
                                                logger.info("adding {} to indexable metadata", identifier.getIRIString());
                                            }
                                        }
                                    } catch (LdpClientException | IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            MapListCollection l = new MapListCollection();
                            l.setMapListCollection(mapList);
                            l.setId(c.getId());
                            l.setLabel(c.getLabel());
                            mapListCollections.add(l);
                        }
                    } catch (LdpClientException | IOException e) {
                        e.printStackTrace();
                    }
                });
                rootCollection.setRootCollection(mapListCollections);
                final String out = serialize(rootCollection).orElse("");
                writeToFile(out, new File("/tmp/ecodices-metadata.json"));
            }
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }
}
