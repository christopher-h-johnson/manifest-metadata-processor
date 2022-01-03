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

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.ubleipzig.metadata.processor.ContextUtils.createInitialContext;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public final class RendererTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RendererTest.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String SPARQL_QUERY = "type";
    private static final String MANIFEST_URI = "manifest";
    private static final String contentTypeJsonLd = "application/ld+json";

    private RendererTest() {
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("About to run Renderer API...");
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        final CamelContext camelContext = new DefaultCamelContext(registry);

        camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                final PropertiesComponent pc = getContext().getComponent("properties", PropertiesComponent.class);

                pc.setLocation("classpath:application.properties");

                from("jetty:http://{{api.host}}:{{api.port}}{{api.prefix}}?"
                        + "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false"
                        + "&httpMethodRestrict=GET,OPTIONS")
                        .routeId("Renderer")
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
                        .filter(header(HTTP_RESPONSE_CODE)
                        .isEqualTo(200))
                        .setHeader(CONTENT_TYPE)
                        .constant(contentTypeJsonLd)
                        .convertBodyTo(String.class)
                        .to("direct:toRDF");
                from("direct:toRDF")
                        .choice()
                        .when(header(SPARQL_QUERY).isEqualTo("pdf"))
                        .process(JsonLdExchange::processMap)
                        .to("file://target")
                        .when(header(SPARQL_QUERY).isEqualTo("count"))
                        .process(JsonLdExchange::getImageCount)
                        .when(header(SPARQL_QUERY).isEqualTo("image"))
                        .process(JsonLdExchange::processImageDownload)
                        .to("file://target");
            }
        });
        camelContext.start();

        Thread.sleep(60 * 60 * 1000);

        camelContext.stop();
    }
}
