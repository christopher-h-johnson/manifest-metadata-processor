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

package de.ubleipzig.metadata.indexer;

import static java.net.http.HttpClient.Redirect.ALWAYS;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.ubleipzig.metadata.processor.JsonSerializer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientImpl;

@Disabled
public class APIIdentifierResolverTest {
    private static Logger logger = LoggerFactory.getLogger(APIIdentifierResolverTest.class);
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();

    private static HttpClient getClient() {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec).followRedirects(ALWAYS).build();
    }

    private List<String> buildIdentifierList(final int start, final int end) {
        final String apiIdentifier = "https://api.digitale-sammlungen.de/iiif/presentation/v2/bsb";
        final HttpClient client = getClient();
        final List<String> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            final String eightDigitString = "%08d";
            final String pid = String.format(eightDigitString, i);
            final IRI identifier = rdf.createIRI(apiIdentifier + pid + "/manifest");
            try {
                final URI uri = new URI(identifier.getIRIString());
                final HttpRequest req = HttpRequest.newBuilder(uri).method("HEAD", noBody()).build();
                final HttpResponse<String> response = client.send(req, ofString());
                logger.info("Identifier {} returned response code {}", identifier.getIRIString(),
                        String.valueOf(response.statusCode()));
                if (response.statusCode() == 200) {
                    list.add(identifier.getIRIString());
                }
            } catch (InterruptedException | IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Test
    void resolveAPIIdentifiers() {
        IntStream.range(1010, 102000 / 10).map(i -> i * 10000).forEach((x) -> {
            final int end = x + 10000;
            final List<String> list = buildIdentifierList(x, end);
            final APIIdentifierCollection collection = new APIIdentifierCollection();
            collection.setIdentifiers(list);
            final String out = JsonSerializer.serialize(collection).orElse("");
            JsonSerializer.writeToFile(out, new File("/tmp/MDZIdentifiers-" + x + ".json"));
        });
    }

    class APIIdentifierCollection {

        @JsonProperty
        private List<String> identifiers;

        public void setIdentifiers(List<String> identifiers) {
            this.identifiers = identifiers;
        }
    }
}
