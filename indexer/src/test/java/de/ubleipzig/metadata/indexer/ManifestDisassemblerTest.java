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

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.AnnotationBodyAtom;
import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.Structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import jdk.incubator.http.HttpResponse;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Test;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

public class ManifestDisassemblerTest {
    private final LdpClient client = new LdpClientImpl();
    private static LdpClient h2client;
    private static final JenaRDF rdf = new JenaRDF();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static LdpClient getClient() {

        final SimpleSSLContext sslct;
        try {
            sslct = new SimpleSSLContext();
            final SSLContext sslContext = sslct.get();
            return h2client = new LdpClientImpl(sslContext);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    void testDisassembleManifest() {
        final IRI manifestIRI = rdf.createIRI("http://gallica.bnf.fr/iiif/ark:/12148/btv1b105102863/manifest.json");
        try {
            h2client = getClient();
            final HttpResponse res = h2client.getResponse(manifestIRI);
            if (res.statusCode() == 200) {
                final String json = res.body().toString();
                final Manifest manifest = MAPPER.readValue(json, new TypeReference<Manifest>() {
                });
                final List<Metadata> metadata = manifest.getMetadata();
                final Map<String, String> metadataMap = new HashMap<>();
                metadata.forEach(m -> {
                    metadataMap.put(m.getLabel(), m.getValue());
                });
                final Optional<List<Structure>> structures = ofNullable(manifest.getStructures());
                final Map<String, List<String>> structureMap = new HashMap<>();
                final Map<String, String> structureLabelMap = new HashMap<>();

                structures.ifPresent(st -> st.forEach(s -> {
                    structureLabelMap.put(s.getStructureId(), s.getStructureLabel());
                    final Optional<List<String>> canvases = ofNullable(s.getCanvases());
                    canvases.ifPresent(strings -> {
                        structureMap.put(s.getStructureId(), strings);
                    });
                }));
                final AtomicInteger ai = new AtomicInteger(1);
                final List<AnnotationBodyAtom> abaList = new ArrayList<>();

                manifest.getSequences().forEach(sq -> {
                    sq.getCanvases().forEach(c -> {
                        final AnnotationBodyAtom aba = new AnnotationBodyAtom();
                        final Integer imageIndex = ai.getAndIncrement();
                        final Optional<Set<String>> structureSet = ofNullable(getKeysByValue(structureMap, c.getId()));
                        final Map<Integer, Structure> sMap = new HashMap<>();
                        final AtomicInteger ai2 = new AtomicInteger(1);
                        structureSet.ifPresent(structs -> structs.forEach(ss -> {
                            final Structure structure = new Structure();
                            structure.setStructureLabel(structureLabelMap.get(ss));
                            structure.setStructureId(ss);
                            sMap.put(ai2.getAndIncrement(), structure);
                        }));
                        aba.setStructureMap(sMap);
                        c.getImages().forEach(i -> {
                            final String iiifService = i.getResource().getService().getId();
                            aba.setIiifService(iiifService);
                            aba.setImageIndex(imageIndex);
                            aba.setMetadata(metadataMap);
                            abaList.add(aba);
                        });
                    });
                });
                final String out = JsonSerializer.serialize(abaList).orElse("");
                System.out.println(out);
            }
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

    public static <T, V> Set<T> getKeysByValue(final Map<T, List<V>> map, final V value) {
        return map.entrySet().stream().filter(entry -> entry.getValue().stream().anyMatch(x -> x.equals(value))).map(
                Map.Entry::getKey).collect(Collectors.toSet());
    }
}
