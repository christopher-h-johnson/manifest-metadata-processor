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

package de.ubleipzig.metadata.extractor.reserializer;

import lombok.extern.slf4j.Slf4j;

import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.baseUrl;
import static de.ubleipzig.metadata.extractor.reserializer.DomainConstants.targetBase;
import static java.io.File.separator;
import static java.io.File.separatorChar;
import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public final class ReserializerUtils {

    private ReserializerUtils() {
    }

    public static Map<String, List<String>> buildLabelMap(final String value, String language) {
        final List<String> labelList = new ArrayList<>();
        labelList.add(value);
        final Map<String, List<String>> labelMap = new HashMap<>();
        labelMap.put(language, labelList);
        return labelMap;
    }

    public static List<String> buildPaddedCanvases(final List<String> canvases, final String viewId) {
        final List<String> paddedCanvases = new ArrayList<>();
        canvases.forEach(c -> {
            try {
                final String paddedCanvasId = format("%08d", Integer.valueOf(new URL(c).getPath().split(String.valueOf(separatorChar))[3]));
                final String canvas = baseUrl + viewId + separator + targetBase + separator + paddedCanvasId;
                paddedCanvases.add(canvas);
            } catch (MalformedURLException e) {
                log.error(e.getMessage());
            }
        });
        return paddedCanvases;
    }
}
