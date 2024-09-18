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

import de.ubleipzig.metadata.processor.ContextUtils;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

import static org.apache.camel.Exchange.*;

public class Renderer {

    private static final String HTTP_ACCEPT = "Accept";
    private static final String SPARQL_QUERY = "type";
    private static final String MANIFEST_URI = "manifest";
    private static final String contentTypeJsonLd = "application/ld+json";

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
        main.bind("x509HostnameVerifier", new NoopHostnameVerifier());
        main.setPropertyPlaceholderLocations("file:${env:RENDERER_HOME}/de.ubleipzig.metadata.renderer.cfg");
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
                    .routeId("Renderer")
                    .removeHeaders(HTTP_ACCEPT)
                    .setHeader("Access-Control-Allow-Origin")
                    .constant("*")
                    .choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .to("direct:getManifest");
            from("direct:getManifest")
                    .process(e -> {
                        e.getIn().setHeader(Exchange.HTTP_URI, e.getIn().getHeader(MANIFEST_URI));
                    })
                    .to("http4")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJsonLd)
                    .convertBodyTo(String.class)
                    .to("direct:toRender");
            from("direct:toRender")
                    .choice()
                    .when(header(SPARQL_QUERY).isEqualTo("count"))
                    .process(JsonLdExchange::getImageCount)
                    .when(header(SPARQL_QUERY).isEqualTo("pdf"))
                    .process(JsonLdExchange::processMap)
                    .to("file://target")
                    .when(header(SPARQL_QUERY).isEqualTo("image"))
                    .process(JsonLdExchange::processImageDownload)
                    .to("file://target");
        }
    }
}
