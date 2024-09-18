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

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ubleipzig.metadata.processor.JsonSerializer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.rdf.api.IRI;
import org.apache.jena.commonsrdf.JenaRDF;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

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

import static java.net.http.HttpClient.Redirect.ALWAYS;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.jsoup.Jsoup.parse;

@Disabled
@Slf4j
public class APIIdentifierResolverTest {
    private static final JenaRDF rdf = new JenaRDF();
    private final LdpClient client = new LdpClientImpl();

    private static HttpClient getClient() {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec).followRedirects(ALWAYS).build();
    }

    private List<String> buildIdentifierList(final int start, final int end) {
        final String apiIdentifier = "https://damsssl.llgc.org.uk/iiif/2.0/";
        final HttpClient client = getClient();
        final List<String> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            final String tenDigitString = "%07d";
            final String pid = String.format(tenDigitString, i);
            final IRI identifier = rdf.createIRI(apiIdentifier + pid + "/manifest.json");
            try {
                final URI uri = new URI(identifier.getIRIString());
                final HttpRequest req = HttpRequest.newBuilder(uri).method("HEAD", noBody()).build();
                final HttpResponse<String> response = client.send(req, ofString());
                log.info("Identifier {} returned response code {}", identifier.getIRIString(),
                        response.statusCode());
                if (response.statusCode() == 200) {
                    list.add(identifier.getIRIString());
                }
            } catch (InterruptedException | IOException | URISyntaxException e) {
                log.error(e.getMessage());
            }
        }
        return list;
    }

    private List<String> buildYaleIdentifierList(final int start, final int end) {
        final String apiIdentifier = "https://manifests.britishart.yale.edu/manifest/";
        final HttpClient client = getClient();
        final List<String> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            final IRI identifier = rdf.createIRI(apiIdentifier + i);
            try {
                final URI uri = new URI(identifier.getIRIString());
                final HttpRequest req = HttpRequest.newBuilder(uri).method("HEAD", noBody()).build();
                final HttpResponse<String> response = client.send(req, ofString());
                log.info("Identifier {} returned response code {}", identifier.getIRIString(),
                        response.statusCode());
                if (response.statusCode() == 200) {
                    list.add(identifier.getIRIString());
                }
            } catch (InterruptedException | IOException | URISyntaxException e) {
                log.error(e.getMessage());
            }
        }
        return list;
    }

    private List<String> buildUBLIdentifierList(final int start, final int end) {
        final String apiIdentifier = "https://iiif.ub.uni-leipzig.de/";
        final HttpClient client = getClient();
        final List<String> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            final String pid = String.format("%010d", i);
            final String id = apiIdentifier + pid + "/manifest.json";
            final IRI identifier = rdf.createIRI(id);
            try {
                final URI uri = new URI(identifier.getIRIString());
                final HttpRequest req = HttpRequest.newBuilder(uri).method("HEAD", noBody()).build();
                final HttpResponse<String> response = client.send(req, ofString());
                log.info("Identifier {} returned response code {}", identifier.getIRIString(),
                        response.statusCode());
                if (response.statusCode() == 200) {
                    list.add(identifier.getIRIString());
                }
            } catch (InterruptedException | IOException | URISyntaxException e) {
                log.error(e.getMessage());
            }
        }
        return list;
    }

    @Test
    void buildAPIIdentifiers() {
            final List<String> list = buildIdentifierList(1128800, 1138800);
            final APIIdentifierCollection collection = new APIIdentifierCollection();
            collection.setIdentifiers(list);
            final String out = JsonSerializer.serialize(collection).orElse("");
            JsonSerializer.writeToFile(out, new File("/tmp/wales-1128800.json"));
    }

    @Test
    void resolveUBLAPIIdentifiers() {
        final List<String> list = buildUBLIdentifierList(1, 13053);
        final APIIdentifierCollection collection = new APIIdentifierCollection();
        collection.setIdentifiers(list);
        final String out = JsonSerializer.serialize(collection).orElse("");
        JsonSerializer.writeToFile(out, new File("/tmp/UBLIdentifiers-20000.json"));
    }

    @Test
    void resolveAPIIdentifiers() {
        IntStream.range(11, 200000 / 10).map(i -> i * 10000).forEach((x) -> {
            final int end = x + 10000;
            final List<String> list = buildIdentifierList(x, end);
            final APIIdentifierCollection collection = new APIIdentifierCollection();
            collection.setIdentifiers(list);
            final String out = JsonSerializer.serialize(collection).orElse("");
            JsonSerializer.writeToFile(out, new File("/tmp/NGAIdentifiers-" + x + ".json"));
        });
    }

    @Setter
    static class APIIdentifierCollection {

        @JsonProperty
        private List<String> identifiers;

    }

    @Test
    void getLCNCollectionFromDOM() {
        final List<String> list = new ArrayList<>();
        final APIIdentifierCollection collection = new APIIdentifierCollection();
        final IRI rootCollectionIRI = rdf.createIRI("https://www.loc.gov/collections/panoramic-maps/?c=500&sp=4&st=list");
        try {
            String response = client.getDefaultType(rootCollectionIRI);
            Document html = parse(response);
            Elements hrefs = html.select("#results > ul > li > div > div > span.item-description-title > a");
            hrefs.forEach(href -> {
                final String id = href.attr("href") + "manifest.json";
                list.add(id);
            });
        } catch (LdpClientException e) {
            log.error(e.getMessage());
        }
        collection.setIdentifiers(list);
        final String out = JsonSerializer.serialize(collection).orElse("");
        JsonSerializer.writeToFile(out, new File("/tmp/LCNMaps-4.json"));
    }
}
