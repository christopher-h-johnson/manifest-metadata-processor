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

import static de.ubleipzig.metadata.processor.JsonSerializer.serialize;
import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixReadMem;

import de.ubleipzig.metadata.templates.ContentList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScannerExchangeProcess {
    private final InputStream image;
    private final String language;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerExchangeProcess.class);

    /**
     * @param image String
     */
    public ScannerExchangeProcess(final InputStream image, final String language) {
        this.image = image;
        this.language = language;
    }

    public String build() {
        BytePointer outText;
        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
        String datapath = null;
        try {
            datapath = new URL(
                    new File(ScannerExchangeProcess.class.getResource("/tessdata").toString()).getParent()).getPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (api.Init(datapath, language) != 0) {
            LOGGER.error("Could not initialize tesseract.");
            throw new RuntimeException("Language not supported");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            image.transferTo(bos);
            byte[] bytes = bos.toByteArray();
            Long size = (long) bytes.length;
            lept.PIX image = pixReadMem(bytes, size);
            api.SetImage(image);
            api.SetSourceResolution(70);
            outText = api.GetHOCRText(0);
            final HocrData hocr = DocManifestBuilder.gethOCRProjection(outText.getString());
            List<String> wordIdList = hocr.getWordNodeId();
            List<ContentList.Content> contentList = new ArrayList<>();
            wordIdList.forEach(w -> {
                final String bbox = DocManifestBuilder.getBboxForId(hocr, w);
                final String chars = DocManifestBuilder.getCharsForId(hocr, w);
                final String region = Region.region().bbox(bbox).build();
                final ContentList.Content content = new ContentList.Content();
                content.setObjectId(w);
                content.setChars(chars);
                content.setRegion(region);
                contentList.add(content);
            });
            final ContentList clist = new ContentList();
            clist.setContentList(contentList);
            final Optional<String> json = serialize(clist);
            api.End();
            outText.deallocate();
            pixDestroy(image);
            return json.orElse(null);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
