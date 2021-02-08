package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.util.DocxTemplate;
import lombok.val;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Service
public class DynamicContentService {
    private final Path docxTemplatePath = FileSystems.getDefault().getPath("templates/printout/room-printout.docx");

    public byte[] getQRCodePNGImage(UriComponents uri, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCode.from(uri.toUriString())
                .withSize(width, height)
                .to(ImageType.PNG)
                .writeTo(out);

        return out.toByteArray();
    }

    public void writeRoomsPrintOutDocx(List<Room> rooms, OutputStream outputStream, Function<Room, UriComponents> uriConverter) throws IOException, InvalidFormatException {
        try(val document = getRoomsPrintOutDox(rooms, uriConverter)) {
           document.write(outputStream);
        }
    }

    private XWPFDocument getRoomsPrintOutDox(List<Room> rooms, Function<Room, UriComponents> uriConverter) throws IOException, InvalidFormatException {
        DocxTemplate.TextTemplate<Room> textReplacer = (room, templatePlaceholder) -> {
            switch (templatePlaceholder) {
                case "g":
                    return room.getBuildingName();
                case "r":
                    return room.getName();
                case "l":
                    return uriConverter.apply(room).toUriString();
                case "p":
                    return Integer.toString(room.getMaxCapacity());
                default:
                    throw new UnsupportedOperationException("Template contains invalid placeholder: " + templatePlaceholder);
            }
        };

        Function<Room, byte[]> qrGenerator = room -> getQRCodePNGImage(uriConverter.apply(room), 500, 500);

        val templateGenerator = new DocxTemplate<>(
                docxTemplatePath.toFile(),
                textReplacer,
                qrGenerator);

        return templateGenerator.generate(rooms);
    }
}
