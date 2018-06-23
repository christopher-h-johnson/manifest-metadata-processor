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

package de.ubleipzig.metadata.scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URL;

import org.junit.jupiter.api.Test;

public class ScannerTest {
    private final String testImage = "https://iiif.ub.uni-leipzig.de/iiif/j2k/0000/0091/0000009199/00000015.jpx";
    private final String fullImageSuffix = "/full/full/0/default.jpg";
    private final String language = "deu_frak";

    @Test
    public void testScanIIIFResource() throws Exception {

        InputStream is = new URL(testImage + fullImageSuffix).openStream();
        final ScannerExchangeProcess builder = new ScannerExchangeProcess(is, testImage, language);
        final String json = builder.build();
        assertTrue(json.contains("Flasche"));
    }
}
