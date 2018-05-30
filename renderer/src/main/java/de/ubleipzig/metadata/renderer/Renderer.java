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

import static de.ubleipzig.metadata.processor.JsonLdProcessorUtils.toRDF;
import static de.ubleipzig.metadata.renderer.PdfDocument.buildPdf;
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

import de.ubleipzig.metadata.processor.ContextUtils;
import de.ubleipzig.metadata.processor.JsonLdProcessorUtils;
import de.ubleipzig.metadata.processor.QueryUtils;

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
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
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

public class Renderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String SPARQL_QUERY = "type";
    private static final String MANIFEST_URI = "manifest";
    private static final String contentTypeJsonLd = "application/ld+json";
    private static final JenaRDF rdf = new JenaRDF();
    private static final String EMPTY = "empty";

    /**
     * main.
     *
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final Renderer selector = new Renderer();
        selector.init();
    }

    /**
     * init.
     *
     * @throws Exception Exception
     */
    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new Renderer.QueryRoute());
        main.addMainListener(new Renderer.Events());
        final JndiRegistry registry = new JndiRegistry(ContextUtils.createInitialContext());
        main.setPropertyPlaceholderLocations("file:${env:DYNAMO_HOME}/de.ubleipzig.metadata.renderer.cfg");
        main.run();
    }

    /**
     * Events.
     */
    public static class Events extends MainListenerSupport {

        @Override
        public void afterStart(final MainSupport main) {
            System.out.println("Renderer is now started!");
        }

        @Override
        public void beforeStop(final MainSupport main) {
            System.out.println("Renderer is now being stopped!");
        }
    }

    /**
     * QueryRoute.
     */
    public static class QueryRoute extends RouteBuilder {


        /**
         * configure.
         */
        public void configure() {
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
                    .process(JsonLdExchange::processJsonLdExchange)
                    .to("file://target");
        }
    }
}