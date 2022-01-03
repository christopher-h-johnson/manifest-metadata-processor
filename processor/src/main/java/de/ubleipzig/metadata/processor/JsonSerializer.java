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

package de.ubleipzig.metadata.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * JsonSerializer.
 *
 * @author christopher-johnson
 */
public final class JsonSerializer {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    static {
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, false);
    }

    private JsonSerializer() {
    }

    /**
     * Serialize the Manifest.
     *
     * @param manifest Object
     * @return the Manifest as a JSON string
     */
    public static Optional<String> serialize(final Object manifest) {
        try {
            return of(MAPPER.writer(PrettyPrinter.instance).writeValueAsString(manifest));
        } catch (final JsonProcessingException ex) {
            return empty();
        }
    }

    /**
     * Serialize the Manifest.
     *
     * @param manifest Object
     * @return the Manifest as a JSON string
     */
    public static Optional<String> serializeRaw(final Object manifest) {
        try {
            return of(MAPPER.writeValueAsString(manifest));
        } catch (final JsonProcessingException ex) {
            return empty();
        }
    }

    /**
     * writeToFile.
     *
     * @param json String
     * @param file File
     * @return Boolean
     */
    public static Boolean writeToFile(final String json, final File file) {
        logger.info("Writing File at {}", file.getPath());
        try (final BufferedWriter writer = newBufferedWriter(file.toPath(), UTF_8, CREATE, APPEND)) {
            writer.write(json);
        } catch (final IOException ex) {
            logger.error("Error writing data to resource {}: {}", file, ex.getMessage());
            return false;
        }
        return true;
    }

    private static class PrettyPrinter extends DefaultPrettyPrinter {

        public static final PrettyPrinter instance = new PrettyPrinter();

        public PrettyPrinter() {
            _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
        }
    }
}
