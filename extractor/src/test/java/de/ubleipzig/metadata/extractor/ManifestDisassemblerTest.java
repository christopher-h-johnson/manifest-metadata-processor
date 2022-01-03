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

import de.ubleipzig.metadata.extractor.disassembler.Disassembler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static de.ubleipzig.metadata.processor.QueryUtils.readFile;

public class ManifestDisassemblerTest {

    @Test
    void testDisassembleManifest() {
        try {
            final URL url = new URL("http://iiif.ub.uni-leipzig.de/0000000005/manifest.json");
            InputStream is = url.openStream();
            String json = readFile(is);
            final Disassembler disassembler = new Disassembler(json);
            System.out.println(disassembler.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

