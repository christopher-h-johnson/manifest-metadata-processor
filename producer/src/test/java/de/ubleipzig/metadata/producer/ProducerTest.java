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

package de.ubleipzig.metadata.producer;

import static java.util.Optional.ofNullable;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.PredicateBuilder.and;

import de.ubleipzig.metadata.processor.ContextUtils;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public final class ProducerTest {

    private static final String HTTP_ACCEPT = "Accept";
    private static final String TYPE = "type";
    private static final String VERSION = "version";
    private static final String URN = "urn";
    private static final String contentTypeXml = "application/xml";
    private static final String contentTypeJsonLd = "application/ld+json";

    private ProducerTest() {
    }

    public static void main(final String[] args) throws Exception {
        log.info("About to run IIIF Producer API...");
        final JndiRegistry registry = new JndiRegistry(ContextUtils.createInitialContext());
        final CamelContext camelContext = new DefaultCamelContext(registry);

        camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                final PropertiesComponent pc = getContext().getComponent("properties", PropertiesComponent.class);

                pc.setLocation("classpath:application.properties");

                errorHandler(defaultErrorHandler().logExhaustedMessageHistory(false));

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
        });
        camelContext.start();

        Thread.sleep(360 * 60 * 1000);

        camelContext.stop();
    }
}
