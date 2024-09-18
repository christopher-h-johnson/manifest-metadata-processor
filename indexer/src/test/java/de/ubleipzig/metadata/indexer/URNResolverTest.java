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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ubleipzig.metadata.processor.JsonSerializer;
import de.ubleipzig.metadata.templates.metsmods.RecordList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.rdf.api.IRI;
import org.apache.jena.commonsrdf.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;
import org.xmlbeam.XBProjector;
import org.xmlbeam.annotation.XBRead;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.xmlbeam.XBProjector.Flags.TO_STRING_RENDERS_XML;

@Disabled
@Slf4j
public class URNResolverTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final XBProjector projector = new XBProjector(TO_STRING_RENDERS_XML);
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();
    private static final Logger LOGGER = LoggerFactory.getLogger(URNResolverTest.class);

    @Test
    void testGetResolvedURLfromURNList() {
        final String resolverService = "http://nbn-resolving.org/process-urn-form?identifier=";
        final List<String> resolverList = buildURNList();
        final List<RecordList.Record> rList = new ArrayList<>();
        final RecordList list = new RecordList();
        resolverList.forEach(urn -> {
            try {
                final String apiLink = resolverService + URLEncoder.encode(
                        urn, StandardCharsets.UTF_8) + "&verb=full&xml=on";
                final IRI req = rdf.createIRI(apiLink);
                final String xml = client.getDefaultType(req);
                NBN nbn = projector.projectXMLString(xml, NBN.class);
                final Optional<String> resolvedURI = ofNullable(getResolvedURI(nbn));
                final RecordList.Record r = new RecordList.Record();
                resolvedURI.ifPresentOrElse(uri -> {
                    r.setUrn(urn);
                    r.setResolvedURI(uri);
                    rList.add(r);
                    final String out = JsonSerializer.serialize(r).orElse("");
                    System.out.println(out);
                }, () -> {
                    LOGGER.error("invalid URN {}", urn);
                });
            } catch (LdpClientException e) {
                log.error(e.getMessage());
            }
        });
        list.setRecords(rList);
        final String out = JsonSerializer.serialize(list).orElse("");
        JsonSerializer.writeToFile(out, new File("/tmp/resolvedURIs.json"));
    }

    @Test
    void testGetResolvedURLfromURN() throws LdpClientException {
        final String resolverService = "http://nbn-resolving.org/process-urn-form?identifier=";
        final String urn = "urn:nbn:de:bsz:15-0008-98541";
        final String suffix = "&verb=full&xml=on";
        final IRI req = rdf.createIRI(resolverService + urn + suffix);
        final String xml = client.getDefaultType(req);
        NBN nbn = projector.projectXMLString(xml, NBN.class);
        final String resolvedURI = getResolvedURI(nbn);
        log.info(resolvedURI);
    }

    public List<String> buildURNList() {
        try {
            final InputStream records = URNResolverTest.class.getResourceAsStream("/data/records.json");
            final RecordList recordList = MAPPER.readValue(records, new TypeReference<RecordList>() {
            });
            final List<String> list = new ArrayList<>();
            recordList.getRecords().forEach(r -> {
                list.add(r.getUrn());
            });
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Record List Api Request Failed" + e.getMessage());
        }
    }

    private interface NBN {
        /**
         * @return String
         */
        @XBRead("//*[local-name()='url']")
        Optional<String> getResolvedURI();

        /**
         * @return String
         */
        @XBRead("//*[local-name()='message']")
        String getMessage();

    }

    private static String getResolvedURI(final NBN nbn) {
        return nbn.getResolvedURI().orElse(null);
    }
}
