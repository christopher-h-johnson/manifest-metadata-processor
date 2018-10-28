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

package de.ubleipzig.metadata.transformer;

import static java.io.File.separator;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;
import de.ubleipzig.metadata.templates.metsmods.RecordList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;

public class XmlDbAccessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlDbAccessor.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JenaRDF rdf = new JenaRDF();
    private final LdpClient client = new LdpClientImpl();
    private String xmldbHost;

    public XmlDbAccessor(final String xmldbHost) {
        this.xmldbHost = xmldbHost;
    }

    public List<Metadata> harmonizeIdentifierLabels(final List<Metadata> metadata) {
        metadata.forEach(m -> {
            final Optional<?> label = ofNullable(m.getLabel());
            final Optional<String> labelString = label.filter(String.class::isInstance).map(String.class::cast);
            labelString.ifPresent(l -> {
                switch (l) {
                    case "urn":
                        m.setLabel("URN");
                        break;
                    case "swb-ppn":
                        m.setLabel("Source PPN (SWB)");
                        break;
                    default:
                        break;
                }
            });
        });
        return metadata;
    }

    public MetsMods getMetadataFromAPI(String urn) {
        final IRI jsonAPI = rdf.createIRI(xmldbHost + "/exist/restxq/mets" + separator + urn);
        final String res;
        try {
            res = client.getDefaultType(jsonAPI);
            return MAPPER.readValue(res, new TypeReference<MetsMods>() {
            });
        } catch (LdpClientException | IOException e) {
            LOGGER.error("URN Api Request Failed for URN {}", urn);
            throw new RuntimeException("URN Api Request Failed" + e.getMessage());
        }
    }

    public MetsMods getMetadataFromAPIwithPPN(String ppn) {
        final IRI jsonAPI = rdf.createIRI(xmldbHost + "/exist/restxq/mets/ppn" + separator + ppn);
        final String res;
        try {
            res = client.getDefaultType(jsonAPI);
            return MAPPER.readValue(res, new TypeReference<MetsMods>() {
            });
        } catch (LdpClientException | IOException e) {
            LOGGER.error("PPN Api Request Failed for PPN {}", ppn);
            throw new RuntimeException("PPN Api Request Failed" + e.getMessage());
        }
    }

    public List<URL> buildMetsModsJsonApiURLList() {
        final IRI jsonAPI = rdf.createIRI(xmldbHost + "/exist/restxq/mets");
        final String res;
        try {
            res = client.getDefaultType(jsonAPI);
            final RecordList recordList = MAPPER.readValue(res, new TypeReference<RecordList>() {
            });
            final List<URL> list = new ArrayList<>();
            recordList.getRecords().forEach(r -> {
                final String apiLink = jsonAPI.getIRIString() + separator + r.getUrn();
                try {
                    list.add(new URL(apiLink));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });
            return list;
        } catch (LdpClientException | IOException e) {
            throw new RuntimeException("Record List Api Request Failed" + e.getMessage());
        }
    }
}
