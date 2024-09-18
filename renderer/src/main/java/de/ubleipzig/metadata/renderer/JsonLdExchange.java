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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.Sequences;
import de.ubleipzig.metadata.templates.ServiceCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static de.ubleipzig.metadata.renderer.RenderedDocument.buildImageZip;
import static de.ubleipzig.metadata.renderer.RenderedDocument.buildPdf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_QUERY;

/**/
@Slf4j
public final class JsonLdExchange {

    private static final String EMPTY = "empty";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonLdExchange() {

    }

    public static void processMap(final Exchange e) throws JsonLdError, URISyntaxException {
        final String queryString = (String) e.getIn().getHeader(HTTP_QUERY);
        final List<NameValuePair> params = URLEncodedUtils.parse(new URI("http://null?" + queryString), UTF_8);
        final Map<String, String> mapped = params.stream().collect(
                Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        final int from = Integer.parseInt(mapped.get("from"));
        final int to = Integer.parseInt(mapped.get("to"));
        final Integer pct = Integer.valueOf(mapped.get("pct"));
        log.info("Getting Json Body");
        final Optional<String> body = ofNullable(e.getIn().getBody().toString());

        if (body.isPresent()) {
            final List<String> imageList = new ArrayList<>();
            final Manifest manifest = mapManifest(body.get());
            final List<Sequences> seqs = manifest.getSequences();
            final String manifestTitle = manifest.getLabel();
            seqs.forEach(s -> s.getCanvases().forEach(c -> {
                c.getImages().forEach(i -> {
                    String imageServiceUri = i.getResource().getService().getId();
                    imageList.add(buildImageServiceRequest(imageServiceUri, pct));

                });
            }));
            final List<String> subList = imageList.subList(from, to);
            log.info("Building PDF with Image Range from {} to {}", from, to);
            setPDFBodyandHeaders(e, subList, manifestTitle);
        } else {
            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
        }
    }

    private static String buildImageServiceRequest(final String imageServiceUri, final Integer pct) {
        return imageServiceUri + "/full/pct:" + pct + "/0/default.jpg";
    }

    private static void setPDFBodyandHeaders(final Exchange e, final List<String> subList, final String manifestTitle) {
        final ByteArrayOutputStream baos;
        try {
            baos = buildPdf(subList);
            e.getIn().setHeader("Content-Type", "application/pdf; name=\"" + manifestTitle + ".pdf\"");
            e.getIn().setHeader("Content-Disposition", "inline; filename=\"" + manifestTitle + "\"");
            e.getIn().setBody(baos.toByteArray());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private static void setImageBodyandHeaders(final Exchange e, final List<String> subList, final String
            manifestTitle) {
        try {
            byte[] zip = buildImageZip(subList, manifestTitle);

            e.getIn().setHeader("Content-Type", "application/zip; name=\"" + manifestTitle + ".zip\"");
            e.getIn().setHeader("Content-Disposition", "attachment; filename=\"" + manifestTitle + "\"");
            e.getIn().setBody(zip);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public static Manifest mapManifest(final String json) {
        try {
            log.info("Mapping Manifest to Object");
            return MAPPER.readValue(json, new TypeReference<Manifest>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void getImageCount(final Exchange e) {
        final Optional<String> body = ofNullable(e.getIn().getBody().toString());
        if (body.isPresent()) {
            final List<String> imageList = new ArrayList<>();
            final Manifest manifest = mapManifest(body.get());
            final List<Sequences> seqs = manifest.getSequences();
            final ServiceCount serviceCount = new ServiceCount();
            seqs.forEach(s -> s.getCanvases().forEach(c -> {
                c.getImages().forEach(i -> {
                    final String imageServiceUri = i.getResource().getService().getId();
                    imageList.add(imageServiceUri);
                });
            }));
            serviceCount.setImageServiceCount(imageList.size());
            final Optional<String> json = serialize(serviceCount);
            e.getIn().setBody(json.orElse(null));
        }
    }

    public static void processImageDownload(final Exchange e) throws JsonLdError, URISyntaxException {
        final String queryString = (String) e.getIn().getHeader(HTTP_QUERY);
        final List<NameValuePair> params = URLEncodedUtils.parse(new URI("http://null?" + queryString), UTF_8);
        final Map<String, String> mapped = params.stream().collect(
                Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        final int from = Integer.parseInt(mapped.get("from"));
        final int to = Integer.parseInt(mapped.get("to"));
        final Integer pct = Integer.valueOf(mapped.get("pct"));
        log.info("Getting Json Body");
        final Optional<String> body = ofNullable(e.getIn().getBody().toString());

        if (body.isPresent()) {
            final List<String> imageList = new ArrayList<>();
            final Manifest manifest = mapManifest(body.get());
            final List<Sequences> seqs = manifest.getSequences();
            final String manifestTitle = manifest.getLabel();
            seqs.forEach(s -> s.getCanvases().forEach(c -> {
                c.getImages().forEach(i -> {
                    String imageServiceUri = i.getResource().getService().getId();
                    imageList.add(buildImageServiceRequest(imageServiceUri, pct));

                });
            }));
            final List<String> subList = imageList.subList(from, to);
            log.info("Building PDF with Image Range from {} to {}", from, to);
            setImageBodyandHeaders(e, subList, manifestTitle);
        } else {
            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
        }
    }
}
