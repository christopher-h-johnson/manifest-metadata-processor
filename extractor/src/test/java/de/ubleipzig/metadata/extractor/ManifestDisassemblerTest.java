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

package de.ubleipzig.metadata.extractor;

import static de.ubleipzig.metadata.processor.QueryUtils.readFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ubleipzig.metadata.extractor.disassembler.Disassembler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.ubleipzig.metadata.templates.v2.PerfectManifest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
public class ManifestDisassemblerTest {

    @Test
    void testDisassembleManifest() {
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000000005/manifest.json");
            InputStream is = url.openStream();
            String json = readFile(is);
            final Disassembler disassembler = new Disassembler(json);
            log.info(disassembler.build());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Test
    void deserializeManifest() {
        final ObjectMapper MAPPER = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true)
                ;
        try {
            final URL url = new URL("https://ids.si.edu/ids/manifest/FS-7491_11");
            InputStream is = url.openStream();
            String json = readFile(is);
            PerfectManifest manifest = MAPPER.readValue(json, new TypeReference<PerfectManifest>() {});
         } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

