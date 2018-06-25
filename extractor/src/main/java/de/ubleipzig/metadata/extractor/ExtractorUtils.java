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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ExtractorUtils {

    private ExtractorUtils() {
    }

    public static final String IIPSRV_DEFAULT = "fcgi-bin/iipsrv.fcgi?iiif=";

    public static <T, V> Set<T> getKeysByValue(Map<T, List<V>> map, V value) {
        return map.entrySet().stream().filter(entry -> entry.getValue().stream().anyMatch(x -> x.equals(value))).map(
                Map.Entry::getKey).collect(Collectors.toSet());
    }
}
