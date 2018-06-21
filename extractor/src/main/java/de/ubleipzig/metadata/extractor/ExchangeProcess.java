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

import static de.ubleipzig.metadata.processor.JsonLdProcessorUtils.toRDF;
import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static java.util.Optional.ofNullable;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.commons.rdf.api.RDFSyntax.NTRIPLES;
import static org.apache.jena.core.rdf.model.ModelFactory.createDefaultModel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.processor.QueryUtils;
import de.ubleipzig.metadata.templates.AnnotationBodyAtom;
import de.ubleipzig.metadata.templates.AtomList;
import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.MetadataMap;
import de.ubleipzig.metadata.templates.Structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.arq.query.Query;
import org.apache.jena.arq.query.QueryExecution;
import org.apache.jena.arq.query.QueryExecutionFactory;
import org.apache.jena.arq.query.QueryFactory;
import org.apache.jena.arq.query.QuerySolution;
import org.apache.jena.arq.query.ResultSet;
import org.apache.jena.arq.riot.Lang;
import org.apache.jena.arq.riot.RDFDataMgr;
import org.apache.jena.core.rdf.model.Literal;
import org.apache.jena.core.rdf.model.Model;
import org.apache.jena.core.rdf.model.ModelFactory;
import org.apache.jena.core.rdf.model.Resource;

public final class ExchangeProcess {

    private static final JenaRDF rdf = new JenaRDF();
    private static final String EMPTY = "empty";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ExchangeProcess() {

    }

    public static void processJsonLdExchange(final Exchange e) throws IOException {
        final String body = e.getIn().getBody().toString();
        if (body != null && !body.isEmpty()) {
            final InputStream is = toRDF(body);
            final Graph graph = getGraph(is);
            final org.apache.jena.core.graph.Graph jenaGraph = rdf.asJenaGraph(Objects.requireNonNull(graph));
            final Model model = ModelFactory.createModelForGraph(jenaGraph);
            final String q = QueryUtils.getQuery("metadata.sparql");
            final Query query = QueryFactory.create(q);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                final ResultSet results = qexec.execSelect();
                final Map<String, String> metadata = new TreeMap<>();
                if (results.hasNext()) {
                    while (results.hasNext()) {
                        final QuerySolution qs = results.next();
                        final Resource id = qs.getResource("manifest");
                        final Literal k = qs.getLiteral("k").asLiteral();
                        final Literal v = qs.getLiteral("mvalue").asLiteral();
                        final Literal l = qs.getLiteral("title").asLiteral();
                        metadata.put(k.getString(), v.getString());
                        metadata.put("Title", l.getString());
                        metadata.put("@id", id.getURI());
                    }
                }
                final MetadataMap metadataMap = new MetadataMap();
                metadataMap.setMetadataMap(metadata);
                final Optional<String> json = serialize(metadataMap);
                e.getIn().setBody(json.orElse(null));
            }
        } else {
            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
        }
    }

    private static Graph getGraph(final InputStream stream) {
        final Model model = createDefaultModel();
        if (rdf.asJenaLang(NTRIPLES).isPresent()) {
            final Lang lang = rdf.asJenaLang(NTRIPLES).get();
            RDFDataMgr.read(model, stream, null, lang);
            return rdf.asGraph(model);
        }
        return null;
    }

    public static void processDisassemblerExchange(final Exchange e) {
        final String body = e.getIn().getBody().toString();
        if (body != null && !body.isEmpty()) {
            try {
                final Manifest manifest = MAPPER.readValue(body, new TypeReference<Manifest>() {
                });
                final List<Metadata> metadata = manifest.getMetadata();
                final Map<String, String> metadataMap = new HashMap<>();

                //set title in metadata
                String title = manifest.getLabel();
                metadataMap.put("Title", title);
                metadata.forEach(m -> {
                    metadataMap.put(m.getLabel(), m.getValue());
                });

                //build structures objects
                final Optional<List<Structure>> structures = ofNullable(manifest.getStructures());
                final Map<String, List<String>> structureMap = new HashMap<>();
                final Map<String, String> structureLabelMap = new HashMap<>();
                structures.ifPresent(st -> st.forEach(s -> {
                    structureLabelMap.put(s.getStructureId(), s.getStructureLabel());
                    Optional<List<String>> canvases = ofNullable(s.getCanvases());
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
                            String iiifService = i.getResource().getService().getId();
                            //hack to fix service
                            if (iiifService.contains("fcgi-bin/iipsrv.fcgi?iiif=")) {
                                iiifService = iiifService.replace(
                                        "fcgi-bin/iipsrv.fcgi?iiif=",
                                        "iiif");
                            }
                            aba.setIiifService(iiifService);
                            aba.setImageIndex(imageIndex);
                            aba.setMetadata(metadataMap);
                            abaList.add(aba);
                        });
                    });
                });
                final AtomList atomList = new AtomList();
                atomList.setAtomList(abaList);
                final Optional<String> json = serialize(atomList);
                e.getIn().setBody(json.orElse(null));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
        }
    }

    public static <T, V> Set<T> getKeysByValue(Map<T, List<V>> map, V value) {
        return map.entrySet().stream().filter(entry -> entry.getValue().stream().anyMatch(x -> x.equals(value))).map(
                Map.Entry::getKey).collect(Collectors.toSet());
    }
}
