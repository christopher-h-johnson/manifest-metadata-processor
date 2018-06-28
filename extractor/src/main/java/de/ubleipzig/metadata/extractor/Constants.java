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

/**
 * Constants.
 */
public final class Constants {
    public static String domainAttribution = "Provided by Leipzig University Library<br/>No Copyright - Public Domain" +
            " Marked";
    public static String domainLogo = "https://iiif.ub.uni-leipzig.de/ubl-logo.png";
    public static String domainLicense = "https://creativecommons.org/publicdomain/mark/1.0/";
    static String baseUrl = "https://iiif.ub.uni-leipzig.de/";
    static String manifestBase = "manifest";
    static String sequenceBase = "sequence";
    static String targetBase = "canvas";
    static String annotationBase = "anno";
    static String viewerUrl = "https://digital.ub.uni-leipzig.de/object/viewid/";
    static String katalogUrl = "https://katalog.ub.uni-leipzig.de/urn/";

    private Constants() {
    }
}
