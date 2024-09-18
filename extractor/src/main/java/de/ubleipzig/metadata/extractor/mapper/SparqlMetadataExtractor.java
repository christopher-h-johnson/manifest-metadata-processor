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

package de.ubleipzig.metadata.extractor.mapper;

//public class SparqlMetadataExtractor {
//    private static final JenaRDF rdf = new JenaRDF();
//    private static final String EMPTY = "empty";
//    private final String body;
//
//    public SparqlMetadataExtractor(final String body) {
//        this.body = body;
//    }
//
//    private static Graph getGraph(final InputStream stream) {
//        final Model model = createDefaultModel();
//        if (rdf.asJenaLang(NTRIPLES).isPresent()) {
//            final Lang lang = rdf.asJenaLang(NTRIPLES).get();
//            RDFDataMgr.read(model, stream, null, lang);
//            return rdf.asGraph(model);
//        }
//        return null;
//    }
//
//    public String build() throws IOException {
//        try {
//            final InputStream is = toRDF(body);
//            final Graph graph = getGraph(is);
//            final org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(Objects.requireNonNull(graph));
//            final Model model = ModelFactory.createModelForGraph(jenaGraph);
//            final String q = QueryUtils.getQuery("metadata.sparql");
//            final Query query = QueryFactory.create(q);
//            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
//                final ResultSet results = qexec.execSelect();
//                final Map<String, Object> metadata = new TreeMap<>();
//                if (results.hasNext()) {
//                    while (results.hasNext()) {
//                        final QuerySolution qs = results.next();
//                        final Resource id = qs.getResource("manifest");
//                        final Literal k = qs.getLiteral("k").asLiteral();
//                        final Literal v = qs.getLiteral("mvalue").asLiteral();
//                        final Literal l = qs.getLiteral("title").asLiteral();
//                        final Resource r = qs.getResource("related");
//                        metadata.put(k.getString(), v.getString());
//                        metadata.put("title", l.getString());
//                        metadata.put("related", r.getURI());
//                        metadata.put("@id", id.getURI());
//                    }
//                }
//                final MetadataMap metadataMap = new MetadataMap();
//                metadataMap.setMetadataMap(metadata);
//                final Optional<String> json = serialize(metadataMap);
//                return json.orElse(null);
//            }
//        } catch (IOException ex) {
//            throw new RuntimeException("Could not Disassemble Manifest", ex.getCause());
//        }
//    }
//}
