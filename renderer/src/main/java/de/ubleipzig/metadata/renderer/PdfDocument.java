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

package de.ubleipzig.metadata.renderer;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PdfDocument {

    public static ByteArrayOutputStream buildPdf(List<String> imageList) throws Exception {
        Image image = new Image(ImageDataFactory.create(new URL(imageList.get(0))));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc, new PageSize(image.getImageWidth(), image.getImageHeight()));
        AtomicInteger ai = new AtomicInteger(0);
        imageList.forEach(i -> {
            Image im = null;
            try {
                im = new Image(ImageDataFactory.create(new URL(i)));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            pdfDoc.addNewPage(new PageSize(Objects.requireNonNull(im).getImageWidth(), im.getImageHeight()));
            im.setFixedPosition(ai.get() + 1, 0, 0);
            doc.add(im);
            ai.incrementAndGet();
        });
        doc.close();
        return baos;
    }
}
