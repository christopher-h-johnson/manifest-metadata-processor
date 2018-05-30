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

package de.ubleipzig.metadata.renderer;

import static de.ubleipzig.metadata.renderer.PdfDocument.buildPdf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_QUERY;
import static org.apache.commons.rdf.api.RDFSyntax.NTRIPLES;
import static org.apache.jena.core.rdf.model.ModelFactory.createDefaultModel;

import com.github.jsonldjava.core.JsonLdError;

import de.ubleipzig.metadata.processor.JsonLdProcessorUtils;
import de.ubleipzig.metadata.processor.QueryUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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

public class JsonLdExchange {

    private static final JenaRDF rdf = new JenaRDF();
    private static final String EMPTY = "empty";

    public static void processJsonLdExchange(final Exchange e) throws IOException, JsonLdError, URISyntaxException {
        final String queryString = (String) e.getIn().getHeader(HTTP_QUERY);
        List<NameValuePair> params = URLEncodedUtils.parse(new URI("http://null?" + queryString), UTF_8);
        Map<String, String> mapped = params.stream().collect(
                Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        Integer from = Integer.valueOf(mapped.get("from"));
        Integer to = Integer.valueOf(mapped.get("to"));

        final String body = e.getIn().getBody().toString();
        if (body != null && !body.isEmpty()) {
            final InputStream is = JsonLdProcessorUtils.toRDF(body);
            final Graph graph = getGraph(is);
            final org.apache.jena.core.graph.Graph jenaGraph = rdf.asJenaGraph(Objects.requireNonNull(graph));
            final Model model = ModelFactory.createModelForGraph(jenaGraph);
            final String q = QueryUtils.getQuery("imageservice.sparql");
            final Query query = QueryFactory.create(q);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                final ResultSet results = qexec.execSelect();
                final Map<String, String> metadata = new TreeMap<>();
                final List<String> titleList = new ArrayList<>();
                if (results.hasNext()) {
                    while (results.hasNext()) {
                        final QuerySolution qs = results.next();
                        final Resource imageId = qs.getResource("imageId");
                        final Resource service = qs.getResource("service");
                        final Literal title = qs.getLiteral("title");
                        metadata.put(imageId.getURI(), service.getURI());
                        titleList.add(title.getString());
                    }
                }
                List<String> imageList = new ArrayList<>();
                metadata.forEach((key, value) -> imageList.add(value + "/full/full/0/default.jpg"));
                List<String> subList = imageList.subList(from, to);
                try {
                    ByteArrayOutputStream baos = buildPdf(subList);
                    e.getIn().setHeader("Content-Type", "application/pdf; name=\"" + titleList.get(0) + ".pdf\"");
                    e.getIn().setHeader("Content-Disposition", "inline; filename=\"" + titleList.get(0) + "\"");
                    e.getIn().setBody(baos.toByteArray());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
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
}
