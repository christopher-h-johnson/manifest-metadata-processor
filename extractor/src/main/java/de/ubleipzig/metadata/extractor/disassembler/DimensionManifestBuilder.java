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
package de.ubleipzig.metadata.extractor.disassembler;

import static de.ubleipzig.metadata.extractor.ExtractorUtils.IIPSRV_DEFAULT;
import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.image.metadata.templates.ImageDimensionManifest;
import de.ubleipzig.image.metadata.templates.ImageDimensions;
import de.ubleipzig.metadata.templates.ImageServiceResponse;
import de.ubleipzig.metadata.templates.Manifest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimensionManifestBuilder {
    private String body;
    private static final Logger LOGGER = LoggerFactory.getLogger(DimensionManifestBuilder.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DimensionManifestBuilder(final String body) {
        this.body = body;
    }

    public String build() {
        try {
            final Manifest manifest = MAPPER.readValue(body, new TypeReference<Manifest>() {
            });
            final ImageDimensionManifest dimManifest = new ImageDimensionManifest();
            dimManifest.setCollection(manifest.getId());
            final List<ImageDimensions> dimList = new ArrayList<>();

            manifest.getSequences().forEach(sq -> {
                sq.getCanvases().forEach(c -> {
                    final ImageDimensions dims = new ImageDimensions();
                    c.getImages().forEach(i -> {
                        String iiifService = i.getResource().getService().getId();
                        //hack to fix service
                        if (iiifService.contains(IIPSRV_DEFAULT)) {
                            iiifService = iiifService.replace(IIPSRV_DEFAULT, "iiif");
                        }
                        //getDimensionsFromImageService
                        InputStream is = null;
                        try {
                            final URL service = new URL(iiifService);
                            final String path = service.getPath();
                            final String filename = FilenameUtils.getName(path);
                            dims.setFilename(filename);
                            is = new URL(iiifService + "/info.json").openStream();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        final ImageServiceResponse ir = mapServiceResponse(is);
                        final Integer height = ir.getHeight();
                        final Integer width = ir.getWidth();
                        dims.setHeight(height);
                        dims.setWidth(width);
                        dimList.add(dims);
                    });
                });
            });
            dimManifest.setImageMetadata(dimList);
            final Optional<String> json = serialize(dimManifest);
            return json.orElse(null);
        } catch (IOException ex) {
            throw new RuntimeException("Could not Disassemble Manifest", ex.getCause());
        }
    }

    /**
     * @param res String
     * @return ImageServiceResponse
     */
    public static ImageServiceResponse mapServiceResponse(final InputStream res) {
        try {
            return MAPPER.readValue(res, new TypeReference<ImageServiceResponse>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
