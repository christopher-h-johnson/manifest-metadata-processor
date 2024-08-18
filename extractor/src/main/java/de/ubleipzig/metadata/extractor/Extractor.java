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

import static de.ubleipzig.metadata.processor.ContextUtils.createInitialContext;
import static de.ubleipzig.metadata.processor.QueryUtils.readFile;
import static java.util.Optional.ofNullable;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.builder.PredicateBuilder.and;

import de.ubleipzig.metadata.extractor.disassembler.DimensionManifestBuilder;
import de.ubleipzig.metadata.extractor.disassembler.Disassembler;
import de.ubleipzig.metadata.extractor.mapper.MetadataMapper;
import de.ubleipzig.metadata.extractor.reserializer.Reserializer;
import de.ubleipzig.metadata.extractor.reserializer.ReserializerVersion3;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Extractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Extractor.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String TYPE = "type";
    private static final String MANIFEST_URI = "m";
    private static final String VERSION = "version";
    private static final String contentTypeJsonLd = "application/ld+json";


    /**
     * main.
     *
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final Extractor selector = new Extractor();
        selector.init();
    }

    /**
     * init.
     *
     * @throws Exception Exception
     */
    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new Extractor.QueryRoute());
        main.addMainListener(new Extractor.Events());
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        main.setPropertyPlaceholderLocations("classpath:de.ubleipzig.metadata.extractor.cfg");
        main.run();
    }

    /**
     * Events.
     */
    public static class Events extends MainListenerSupport {

        @Override
        public void afterStart(final MainSupport main) {
            System.out.println("Extractor is now started!");
        }

        @Override
        public void beforeStop(final MainSupport main) {
            System.out.println("Extractor is now being stopped!");
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
                    .setHeader(
                    "Access-Control-Allow" + "-Origin")
                    .constant("*").choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET")).to("direct:getManifest");
            from("direct:getManifest")
                    .process(e -> {
                        final String m = e.getIn().getHeader(MANIFEST_URI).toString();
                        final URI uri = new URI(m);
                        final String query = uri.getQuery();
                        final String path = uri.getPath();
                        final String authority = uri.getAuthority();
                        final String scheme = uri.getScheme();
                        final String req = scheme + "://" + authority + path;
                        e.getIn().setHeader(Exchange.HTTP_URI, req);
                        e.getIn().setHeader(Exchange.HTTP_QUERY, query);
                    })
                    .setHeader("Accept-Encoding")
                    .constant("gzip")
                    .to("http4")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJsonLd)
                    .convertBodyTo(InputStream.class)
                    .log(INFO, LOGGER, "Fetching Json-LD document")
                    .to("direct:toExchangeProcess");
            from("direct:toExchangeProcess")
                    .choice()
                    .when(header(TYPE).isEqualTo("extract"))
                    .process(e -> {
                            final Optional<InputStream> is = ofNullable(e.getIn().getBody(InputStream.class));
                            if (is.isPresent()) {
                                final InputStream bis = is.get();
                                final String body = readFile(bis);
                                final MetadataMapper extractor = new MetadataMapper(body);
                                e.getIn().setBody(extractor.build());
                            }
                    })
                    .when(header(TYPE).isEqualTo("disassemble"))
                    .process(e -> {
                        final Optional<InputStream> is = ofNullable(e.getIn().getBody(InputStream.class));
                        if (is.isPresent()) {
                            final InputStream bis = is.get();
                            final String body = readFile(bis);
                            final Disassembler disassembler = new Disassembler(body);
                            e.getIn().setBody(disassembler.build());
                        }
                    })
                    .when(header(TYPE).isEqualTo("dimensions"))
                    .process(e -> {
                            final Optional<InputStream> is = ofNullable(e.getIn().getBody(InputStream.class));
                            if (is.isPresent()) {
                                final InputStream bis = is.get();
                                final String body = readFile(bis);
                                final DimensionManifestBuilder dimManifestBuilder = new DimensionManifestBuilder(body);
                                e.getIn().setBody(dimManifestBuilder.build());
                            }
                    })
                    .when(and(header(TYPE).isEqualTo("reserialize"), header(VERSION).isEqualTo("2")))
                    .process(e -> {
                        final Optional<InputStream> is = ofNullable(e.getIn().getBody(InputStream.class));
                        final String xmldbHost = e.getContext().resolvePropertyPlaceholders("{{xmldb.host}}");
                            if (is.isPresent()) {
                                final InputStream bis = is.get();
                                final String body = readFile(bis);
                                final Reserializer reserializer = new Reserializer(body, xmldbHost);
                                e.getIn().setBody(reserializer.build());
                            }
                    })
                    .when(and(header(TYPE).isEqualTo("reserialize"), header(VERSION).isEqualTo("3")))
                    .process(e -> {
                        final Optional<InputStream> is = ofNullable(e.getIn().getBody(InputStream.class));
                        final String xmldbHost = e.getContext().resolvePropertyPlaceholders("{{xmldb.host}}");
                            if (is.isPresent()) {
                                final InputStream bis = is.get();
                                final String body = readFile(bis);
                                final ReserializerVersion3 reserializer = new ReserializerVersion3(body, xmldbHost);
                                e.getIn().setBody(reserializer.build());
                            }
                    });
        }
    }
}
