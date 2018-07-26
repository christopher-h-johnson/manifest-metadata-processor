package de.ubleipzig.metadata.producer;

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


import static de.ubleipzig.metadata.processor.ContextUtils.createInitialContext;
import static java.util.Optional.ofNullable;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.builder.PredicateBuilder.and;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String TYPE = "type";
    private static final String VERSION = "version";
    private static final String URN = "urn";
    private static final String contentTypeXml = "application/xml";
    private static final String contentTypeJsonLd = "application/ld+json";


    /**
     * main.
     *
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final Producer producer = new Producer();
        producer.init();
    }

    /**
     * init.
     *
     * @throws Exception Exception
     */
    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new Producer.QueryRoute());
        main.addMainListener(new Producer.Events());
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        main.setPropertyPlaceholderLocations("file:${env:PRODUCER_HOME}/de.ubleipzig.metadata.producer.cfg");
        main.run();
    }

    /**
     * Events.
     */
    public static class Events extends MainListenerSupport {

        @Override
        public void afterStart(final MainSupport main) {
            System.out.println("Producer is now started!");
        }

        @Override
        public void beforeStop(final MainSupport main) {
            System.out.println("Producer is now being stopped!");
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
                    .routeId("Producer")
                    .removeHeaders(HTTP_ACCEPT)
                    .setHeader(
                            "Access-Control-Allow-Origin")
                    .constant("*")
                    .choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .to("direct:getMetsMods");
            from("direct:getMetsMods")
                    .process(e -> {
                        final String xmldbHost = e.getContext().resolvePropertyPlaceholders("{{xmldb.host}}");
                        final String urnContextPath =
                                e.getContext().resolvePropertyPlaceholders("{{urn.lookup.context}}");
                        final String xmlURI = xmldbHost + urnContextPath + "?urn=" + e.getIn().getHeader(URN);
                        e.getIn().setHeader(Exchange.HTTP_URI, xmlURI);
                    })
                    .to("http4")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeXml)
                    .convertBodyTo(String.class)
                    .log(INFO, LOGGER, "Fetching XML document")
                    .to("direct:toExchangeProcess");
            from("direct:toExchangeProcess")
                    .choice()
                    .when(and(header(TYPE).isEqualTo("produce"), header(VERSION).isEqualTo("2")))
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJsonLd)
                    .process(e -> {
                        final Optional<String> body = ofNullable(e.getIn().getBody().toString());
                        final String xmldbHost = e.getContext().resolvePropertyPlaceholders("{{xmldb.host}}");
                        if (body.isPresent()) {
                            final ProducerBuilderVersion2 builder =
                                    new ProducerBuilderVersion2(body.get(), xmldbHost);
                            e.getIn().setBody(builder.build());
                        }
                    });
        }
    }
}
