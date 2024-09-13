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

import static java.util.Optional.ofNullable;

import de.ubleipzig.metadata.templates.Manifest;
import de.ubleipzig.metadata.templates.Metadata;
import de.ubleipzig.metadata.templates.metsmods.MetsMods;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataBuilder.class);
    private MetadataApi<Metadata> metadataImplVersion2 = new MetadataImplVersion2();
    private final Manifest manifest;
    private final XmlDbAccessor accessor;

    public MetadataBuilder(final Manifest manifest, final String xmldbHost) {
        this.manifest = manifest;
        this.accessor = new XmlDbAccessor(xmldbHost);
    }

    public MetadataApi<Metadata> build() {
        final Optional<List<Metadata>> metadata = ofNullable(manifest.getMetadata());
        if (metadata.isPresent()) {
            List<Metadata> harmonizedMetadata = accessor.harmonizeIdentifierLabels(metadata.get());
            final Optional<Metadata> metaURN = harmonizedMetadata.stream().filter(
                    y -> y.getLabel().equals("URN")).findAny();
            final Optional<Metadata> metaPPN = harmonizedMetadata.stream().filter(
                    y -> y.getLabel().equals("Source PPN (SWB)")).findAny();
            if (metaURN.isPresent()) {
                final Optional<?> value = ofNullable(metaURN.get().getValue());
                final Optional<String> urn = value.filter(String.class::isInstance).map(String.class::cast);
                if (urn.isPresent()) {
                    if (urn.get().equals("null") && metaPPN.isPresent()) {
                        final Optional<?> value2 = ofNullable(metaPPN.get().getValue());
                        final Optional<String> ppn = value2.filter(String.class::isInstance).map(String.class::cast);
                        ppn.ifPresent(s -> metadataImplVersion2 = buildMetadataFromPPNApi(s));
                    } else {
                        metadataImplVersion2 = buildMetadataFromURNApi(urn.get());
                    }
                }
            } else if (metaPPN.isPresent()) {
                final Optional<?> value2 = ofNullable(metaPPN.get().getValue());
                final Optional<String> ppn = value2.filter(String.class::isInstance).map(String.class::cast);
                ppn.ifPresent(s -> metadataImplVersion2 = buildMetadataFromPPNApi(ppn.get()));
            } else {
                throw new RuntimeException("no valid identifiers for manifest");
            }
        }
        return metadataImplVersion2;
    }

    public MetadataApi<Metadata> buildMetadataFromPPNApi(String ppn) {
        LOGGER.info("Getting Metadata from PPN API using {} for manifest {}", ppn, manifest.getId());
        final Optional<MetsMods> metsMods = ofNullable(accessor.getMetadataFromAPIwithPPN(ppn));
        metsMods.ifPresentOrElse(mets -> {
            metadataImplVersion2.setMetsMods(mets);
            metadataImplVersion2.buildFinalMetadata();
        }, () -> {
            LOGGER.error("invalid PPN {} for manifest {}", ppn, manifest.getId());
            throw new RuntimeException("Invalid PPN for manifest");
        });
        return metadataImplVersion2;
    }

    public MetadataApi<Metadata> buildMetadataFromURNApi(String urn) {

        LOGGER.info("Getting Metadata from URN API using {} for manifest {}", urn, manifest.getId());
        final Optional<MetsMods> metsMods = ofNullable(accessor.getMetadataFromAPI(urn));
        metsMods.ifPresentOrElse(mets -> {
            metadataImplVersion2.setMetsMods(mets);
            metadataImplVersion2.buildFinalMetadata();
        }, () -> {
            LOGGER.error("invalid URN {} for manifest {}", urn, manifest.getId());
            throw new RuntimeException("Invalid URN for manifest");
        });
        return metadataImplVersion2;
    }

}
