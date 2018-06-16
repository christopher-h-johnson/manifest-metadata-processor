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

import static de.ubleipzig.metadata.processor.JsonSerializer.MAPPER;
import static de.ubleipzig.metadata.processor.QueryUtils.readFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import de.ubleipzig.metadata.templates.ElasticCreate;
import de.ubleipzig.metadata.templates.ElasticDocumentObject;
import de.ubleipzig.metadata.templates.ElasticIndex;
import de.ubleipzig.metadata.templates.MetadataMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import jdk.incubator.http.HttpResponse;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.trellisldp.client.LdpClient;
import org.trellisldp.client.LdpClientException;
import org.trellisldp.client.LdpClientImpl;


public class Indexer {
    private final LdpClient client = new LdpClientImpl();
    private static final JenaRDF rdf = new JenaRDF();

    public Indexer() {
    }

    public ElasticIndex createIndex(String indexName, String type, String id) {
        final ElasticDocumentObject index = new ElasticDocumentObject();
        index.setIndex(indexName);
        index.setType(type);
        index.setId(id);
        final ElasticIndex i = new ElasticIndex();
        i.setIndex(index);
        return i;
    }

    public ElasticCreate createDocument(String indexName, String type, String id) {
        final ElasticDocumentObject doc = new ElasticDocumentObject();
        doc.setIndex(indexName);
        doc.setType(type);
        doc.setId(id);
        final ElasticCreate create = new ElasticCreate();
        create.setCreate(doc);
        return create;
    }

    public void indexJson(String baseUrl, String documentId, String indexName, String indexType, String json) {
        final IRI identifier = rdf.createIRI(baseUrl + indexName + indexType + "/" + documentId);
        InputStream is = new ByteArrayInputStream(json.getBytes());
        try {
            client.put(identifier, is, "application/json");
        } catch (LdpClientException e) {
            e.printStackTrace();
        }
    }

    public void createIndexMapping(String baseUrl, InputStream mapping) {
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode jsonNode = MAPPER.readValue(readFile(mapping), JsonNode.class);
            sb.append(jsonNode.toString());
            System.out.println(sb.toString());
            InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
            client.put(rdf.createIRI(baseUrl), is, "application/json");
        } catch (LdpClientException | IOException e) {
            e.printStackTrace();
        }
    }

    public MetadataMap getMetadataMap(IRI iri) {
        try {
            final String extractorService = "http://localhost:9098/extractor?type=extract&manifest=";
            final String extractorRequest = extractorService + iri.getIRIString();
            final IRI req = rdf.createIRI(extractorRequest);
            final HttpResponse res = client.getResponse(req);
            if (res.statusCode() == 200) {
                final String json = res.body().toString();
                return MAPPER.readValue(json, new TypeReference<MetadataMap>() {
                });
            }
        } catch (LdpClientException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }
}
