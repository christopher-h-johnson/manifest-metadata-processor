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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xmlbeam.XBProjector;

/**
 * DocManifestBuilder.
 *
 * @author Christopher Johnson
 */
public final class DocManifestBuilder {

    private DocManifestBuilder() {
    }

    /**
     * gethOCRProjectionFromURL.
     *
     * @param hocr String
     * @return XBProjector
     * @throws IOException Exception
     */
    public static HocrData gethOCRProjection(final String hocr) throws IOException {
        XBProjector projector = new XBProjector();
        return projector.projectXMLString(hocr, HocrData.class);
    }

    /**
     * buildValueMap.
     *
     * @param descList List
     * @param hocr     HocrData
     * @return valueMap
     */
    private static Map<String, Object> buildValueMap(final List<String> descList, final HocrData hocr) {
        final Map<String, Object> valueMap = new HashMap<>();
        for (final String descId : descList) {
            final Object oNode = hocr.getTitleForId(descId);
            valueMap.put(descId, oNode);
        }
        return valueMap;
    }

    /**
     * getAreaMapForhOCRResource.
     *
     * @param hocr HocrData
     * @return areaMap
     */
    public static Map getAreaMapForhOCRResource(final HocrData hocr) {
        final List<String> cAreaIdList = hocr.getCAreaNodeId();
        final Map<String, Object> areaMap = new HashMap<>();
        for (final String cAreaId : cAreaIdList) {
            final List<String> descList = hocr.getCAreaIdDescIds(cAreaId);
            areaMap.put(cAreaId, buildValueMap(descList, hocr));
        }
        return areaMap;
    }

    /**
     * getPageIdList.
     *
     * @param hocr HocrData
     * @return PageNodeId
     */
    public static List<String> getPageIdList(final HocrData hocr) {
        return hocr.getPageNodeId();
    }

    /**
     * getAreaIdList.
     *
     * @param hocr HocrData
     * @return CAreaNodeId
     */
    public static List<String> getAreaIdList(final HocrData hocr) {
        return hocr.getCAreaNodeId();
    }

    /**
     * getLineIdList.
     *
     * @param hocr HocrData
     * @return LineNodeId
     */
    public static List<String> getLineIdList(final HocrData hocr) {
        return hocr.getLineNodeId();
    }

    /**
     * getWordIdList.
     *
     * @param hocr HocrData
     * @return WordNodeId
     */
    public static List<String> getWordIdList(final HocrData hocr) {
        return hocr.getWordNodeId();
    }

    /**
     * getAreaIdListforPage.
     *
     * @param hocr HocrData
     * @param id   String
     * @return CAreasforPage
     */
    public static List<String> getAreaIdListforPage(final HocrData hocr, final String id) {
        return hocr.getCAreasforPage(id);
    }

    /**
     * getLineIdListforArea.
     *
     * @param hocr HocrData
     * @param id   String
     * @return LinesforArea
     */
    public static List<String> getLineIdListforArea(final HocrData hocr, final String id) {
        return hocr.getLinesforArea(id);
    }

    /**
     * getBboxForId.
     *
     * @param hocr HocrData
     * @param id   String
     * @return TitleForId
     */
    public static String getBboxForId(final HocrData hocr, final String id) {
        return StringUtils.substringBefore(StringUtils.substringAfter(hocr.getTitleForId(id), "bbox "), ";");
    }

    /**
     * getCharsForId.
     *
     * @param hocr HocrData
     * @param id   String
     * @return CharsForId
     */
    public static String getCharsForId(final HocrData hocr, final String id) {
        return hocr.getCharsForId(id);
    }

    /**
     * getWordIdListforLine.
     *
     * @param hocr HocrData
     * @param id   String
     * @return WordsforLine
     */
    public static List<String> getWordIdListforLine(final HocrData hocr, final String id) {
        return hocr.getWordsforLine(id);
    }

    /**
     * getWordIdListforPage.
     *
     * @param hocr HocrData
     * @param id   String
     * @return WordsforPage
     */
    public static List<String> getWordIdListforPage(final HocrData hocr, final String id) {
        return hocr.getWordsforPage(id);
    }
}