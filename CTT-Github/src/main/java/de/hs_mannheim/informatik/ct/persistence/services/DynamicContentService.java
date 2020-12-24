package de.hs_mannheim.informatik.ct.persistence.services;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import de.hs_mannheim.informatik.ct.model.Room;
import lombok.NonNull;
import lombok.val;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Service
public class DynamicContentService {
    private final Path templatePath = FileSystems.getDefault().getPath("templates/printout/formTemplate.pdf");

    public byte[] getQRCodePNGImage(UriComponents uri, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCode.from(uri.toUriString())
                .withSize(width, height)
                .to(ImageType.PNG)
                .writeTo(out);

        return out.toByteArray();
    }

    public void writeRoomsPrintoutPDF(List<Room> rooms, OutputStream outputStream, Function<Room, UriComponents> uriConverter) throws IOException, DocumentException {
        val templateBuffer = Files.readAllBytes(templatePath);
        val document = new Document();
        val copy = new PdfSmartCopy(document, outputStream);
        document.open();
        try {
            rooms
                    .parallelStream()
                    .map(room -> {
                        try {
                            return getRoomsPrintOutPDF(uriConverter, templateBuffer, room);

                        } catch (IOException | DocumentException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEachOrdered(buffer -> {
                        try {
                            val pdfPage = new PdfReader(buffer);
                            copy.addDocument(pdfPage);
                            pdfPage.close();
                        } catch (IOException | DocumentException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e.getCause() instanceof DocumentException) {
                throw (DocumentException) e.getCause();
            } else {
                throw e;
            }
        }

        document.close();
    }

    @NonNull
    private byte[] getRoomsPrintOutPDF(Function<Room, UriComponents> uriConverter, byte[] templateBuffer, Room room) throws IOException, DocumentException {
        val reader = new PdfReader(templateBuffer);
        val writer = new ByteArrayOutputStream();
        val stamper = new PdfStamper(reader, writer);
        val form = stamper.getAcroFields();
        val uri = uriConverter.apply(room);
        form.setField("NameHeader", room.getName());
        form.setFieldProperty("NameHeader", "textsize", 28f, null);
        form.regenerateField("NameHeader");
        form.setField("ShortURL", uri.toUriString());
        form.setFieldProperty("ShortURL", "textsize", 16f, null);
        form.regenerateField("ShortURL");

        setFormButtonFieldImage(form, "QRCodeImage", uri);

        form.setGenerateAppearances(true);
        stamper.setFormFlattening(true);
        stamper.flush();
        stamper.close();
        reader.close();
        val buffer = writer.toByteArray();
        writer.close();
        return buffer;
    }

    private void setFormButtonFieldImage(AcroFields form, String fieldName, UriComponents uri) throws IOException, DocumentException {
        val qrImageField = form.getNewPushbuttonFromField(fieldName);
        qrImageField.setLayout(PushbuttonField.LAYOUT_ICON_ONLY);
        qrImageField.setProportionalIcon(true);
        val qrImage = Image.getInstance(getQRCodePNGImage(uri, 1000, 1000));
        qrImageField.setImage(qrImage);
        form.replacePushbuttonField(fieldName, qrImageField.getField());
    }
}
