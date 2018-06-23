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

package de.ubleipzig.metadata.scanner;

import static de.ubleipzig.metadata.processor.ContextUtils.createInitialContext;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;

import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scanner.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String TYPE = "type";
    private static final String IMAGE_URI = "image";
    private static final String LANGUAGE = "lang";
    private static final String contentTypeImageJPEG = "image/jpeg";
    private static final String contentTypeJson = "application/json";
    private static final String fullImageSuffix = "/full/full/0/default.jpg";

    /**
     * main.
     *
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final Scanner scanner = new Scanner();
        scanner.init();
    }

    /**
     * init.
     *
     * @throws Exception Exception
     */
    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new Scanner.QueryRoute());
        main.addMainListener(new Scanner.Events());
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        main.setPropertyPlaceholderLocations("classpath:de.ubleipzig.metadata.scanner.cfg");
        main.run();
    }

    /**
     * Events.
     */
    public static class Events extends MainListenerSupport {

        @Override
        public void afterStart(final MainSupport main) {
            System.out.println("Scanner is now started!");
        }

        @Override
        public void beforeStop(final MainSupport main) {
            System.out.println("Scanner is now being stopped!");
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
                    .routeId("Scanner")
                    .removeHeaders(HTTP_ACCEPT)
                    .setHeader("Access-Control-Allow-Origin")
                    .constant("*").choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .to("direct:scan");
            from("direct:scan")
                    .process(e -> e.getIn().setHeader(Exchange.HTTP_URI, e.getIn().getHeader(IMAGE_URI)
                            + fullImageSuffix))
                    .to("http4")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeImageJPEG)
                    .convertBodyTo(InputStream.class)
                    .log(INFO, LOGGER, "Fetching Image")
                    .to("direct:toExchangeProcess");
            from("direct:toExchangeProcess")
                    .choice()
                    .when(header(TYPE).isEqualTo("scan"))
                    .process(e -> {
                        final InputStream is = e.getIn().getBody(InputStream.class);
                        final String language = (String) e.getIn().getHeader(LANGUAGE);
                        final ScannerExchangeProcess builder = new ScannerExchangeProcess(is, language);
                        e.getIn().setBody(builder.build());
                    })
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJson);
        }
    }
}
