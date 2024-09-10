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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.utils.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonLdProcessorUtils.
 *
 * @author christopher-johnson
 */
public final class JsonLdProcessorUtils {

    private JsonLdProcessorUtils() {
    }

    /**
     * toRDF.
     *
     * @param jsonLd String
     * @return InputStream
     * @throws JsonLdError JsonLdError
     */
    public static InputStream toRDF(final String jsonLd) {
        final JsonLdOptions options = new JsonLdOptions();
        options.format = JsonLdConsts.APPLICATION_NQUADS;
        try {
            final Object expanded = com.github.jsonldjava.core.JsonLdProcessor.toRDF(
                    JsonUtils.fromString(jsonLd), options);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final Writer writer = new OutputStreamWriter(out, UTF_8);

            writer.write(String.valueOf(expanded));
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
