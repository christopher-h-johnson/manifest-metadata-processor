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

package de.ubleipzig.metadata.processor;

import static de.ubleipzig.metadata.processor.JsonLdProcessorUtils.toRDF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_QUERY;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.commons.rdf.api.RDFSyntax.NTRIPLES;
import static org.apache.jena.core.rdf.model.ModelFactory.createDefaultModel;

import com.github.jsonldjava.core.JsonLdError;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.util.IOHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RendererTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Extractor.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String SPARQL_QUERY = "type";
    private static final String MANIFEST_URI = "manifest";
    private static final String contentTypeJsonLd = "application/ld+json";
    private static final JenaRDF rdf = new JenaRDF();
    private static final String EMPTY = "empty";

    private RendererTest() {
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("About to run Metadata Extractor API...");
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        final CamelContext camelContext = new DefaultCamelContext(registry);

        camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                final PropertiesComponent pc = getContext().getComponent("properties", PropertiesComponent.class);

                pc.setLocation("classpath:application.properties");

                from("jetty:http://{{api.host}}:{{api.port}}{{api.prefix}}?"
                        + "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false"
                        + "&httpMethodRestrict=GET,OPTIONS")
                        .routeId("Extractor")
                        .removeHeaders(HTTP_ACCEPT)
                        .setHeader("Access-Control-Allow-Origin")
                        .constant("*")
                        .choice()
                        .when(header(HTTP_METHOD)
                        .isEqualTo("GET"))
                        .to("direct:getManifest");
                from("direct:getManifest")
                        .process(e -> {
                             e.getIn().setHeader(Exchange.HTTP_URI, e.getIn().getHeader(MANIFEST_URI));
                           })
                        .to("http4")
                        .log("headers = ${headers}")
                        .filter(header(HTTP_RESPONSE_CODE)
                        .isEqualTo(200))
                        .setHeader(CONTENT_TYPE)
                        .constant(contentTypeJsonLd)
                        .convertBodyTo(String.class)
                        .to("direct:toRDF");
                from("direct:toRDF")
                        .choice()
                        .when(header(SPARQL_QUERY)
                        .isEqualTo("extract"))
                        .process(RendererTest::processJsonLdExchange)
                        .to("file://target");
            }
        });
        camelContext.start();

        Thread.sleep(60 * 60 * 1000);

        camelContext.stop();
    }

    private static void processJsonLdExchange(final Exchange e) throws IOException, JsonLdError, URISyntaxException {
        final String queryString = (String) e.getIn().getHeader(HTTP_QUERY);
        List<NameValuePair> params = URLEncodedUtils.parse(new URI("http://null?" + queryString), UTF_8);
        Map<String, String> mapped = params.stream().collect(
                Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        Integer from = Integer.valueOf(mapped.get("from"));
        Integer to = Integer.valueOf(mapped.get("to"));

        final String body = e.getIn().getBody().toString();
        if (body != null && !body.isEmpty()) {
            final InputStream is = toRDF(body);
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
                    e.getIn().setHeader("Content-Disposition", "inline; filename=\"" + titleList.get(0) +"\"");
                    e.getIn().setBody(baos.toByteArray());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
        }
    }

    private static ByteArrayOutputStream buildPdf(List<String> imageList) throws Exception {
        Image image = new Image(ImageDataFactory.create(new URL(imageList.get(0))));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc, new PageSize(image.getImageWidth(), image.getImageHeight()));
        AtomicInteger ai = new AtomicInteger(0);
        imageList.forEach(i -> {
            Image im = null;
            try {
                im = new Image(ImageDataFactory.create(new URL(i)));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            pdfDoc.addNewPage(new PageSize(Objects.requireNonNull(im).getImageWidth(), im.getImageHeight()));
            im.setFixedPosition(ai.get() +1, 0, 0);
            doc.add(im);
            ai.incrementAndGet();
        });
        doc.close();
        return baos;
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

    /**
     * createInitialContext.
     *
     * @return InitialContext Context
     * @throws Exception Exception
     */
    private static Context createInitialContext() throws Exception {
        final InputStream in = RendererTest.class.getClassLoader().getResourceAsStream("jndi.properties");
        try {
            final Properties properties = new Properties();
            properties.load(in);
            return new InitialContext(new Hashtable<>(properties));
        } finally {
            IOHelper.close(in);
        }
    }
}