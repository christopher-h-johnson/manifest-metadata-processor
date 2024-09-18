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
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.util.IOHelper;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public final class RenderedDocument {

    private RenderedDocument() {
    }

    public static ByteArrayOutputStream buildPdf(List<String> imageList) throws Exception {
        final Image image = new Image(ImageDataFactory.create(new URI(imageList.get(0)).toURL()));
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(new PdfWriter(baos));
        final Document doc = new Document(pdfDoc, new PageSize(image.getImageWidth(), image.getImageHeight()));
        final AtomicInteger ai = new AtomicInteger(0);
        imageList.forEach(i -> {
            Image im = null;
            try {
                im = new Image(ImageDataFactory.create(new URI(i).toURL()));
            } catch (MalformedURLException e) {
               log.error(e.getMessage());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            pdfDoc.addNewPage(new PageSize(Objects.requireNonNull(im).getImageWidth(), im.getImageHeight()));
            im.setFixedPosition(ai.get() + 1, 0, 0);
            doc.add(im);
            ai.incrementAndGet();
        });
        doc.close();
        return baos;
    }

    public static byte[] buildImageZip(List<String> imageList, String manifestTitle) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        final AtomicInteger ai = new AtomicInteger(0);
        imageList.forEach(i -> {
            try {
                final InputStream is = new URI(i).toURL().openStream();
                byte[] targetArray = IOUtils.toByteArray(is);
                zos.putNextEntry(new ZipEntry(manifestTitle + ai.get() + ".jpg"));
                zos.write(targetArray);
                zos.closeEntry();
                ai.getAndIncrement();
            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        IOHelper.close(zos);
        return baos.toByteArray();
    }
}
