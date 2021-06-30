package de.hs_mannheim.informatik.ct.persistence.services;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import de.hs_mannheim.informatik.ct.controller.Utilities;
import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.util.ContactListGenerator;
import de.hs_mannheim.informatik.ct.util.DocxTemplate;
import javassist.bytecode.ByteArray;
import lombok.val;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.io.IOException;  // Import the IOException class to handle errors
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DynamicContentService {
    @Autowired
    private Utilities utilities;

    private final Path defaultDocxTemplatePath = FileSystems.getDefault().getPath("templates/printout/room-printout.docx");
    private final Path privilegedDocxTemplatePath = FileSystems.getDefault().getPath("templates/printout/room-printout-privileged.docx");

    public byte[] getQRCodePNGImage(UriComponents uri, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCode.from(uri.toUriString())
                .withSize(width, height)
                .to(ImageType.PNG)
                .writeTo(out);

        return out.toByteArray();
    }

    public void writeRoomsPrintOutDocx(List<Room> rooms, ZipOutputStream outputStream, Function<Room, UriComponents> uriConverter) throws IOException, XmlException {
        List<XWPFDocument> ListOfDocuments = getRoomsPrintOutDox(rooms, uriConverter, false);

        int counter = 0;
        outputStream.putNextEntry(new ZipEntry(rooms.get(0).getBuildingName() + "/"));
        for (XWPFDocument document : ListOfDocuments) {

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.write(out);
                out.close();
                byte[] xwpfDocumentBytes = out.toByteArray();
                outputStream.putNextEntry(new ZipEntry(rooms.get(0).getBuildingName() + "/" + rooms.get(counter).getName() + ".docx"));
                outputStream.write(xwpfDocumentBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        }
    }

    public void writeContactList(Collection<Contact<?>> contacts, Visitor target, ContactListGenerator generator, OutputStream outputStream) throws IOException {
        try (val workbook = generator.generateWorkbook(contacts, target.getEmail())) {
            workbook.write(outputStream);
        }
    }

    public List<XWPFDocument> getRoomsPrintOutDox(List<Room> rooms, Function<Room, UriComponents> uriConverter, boolean privileged) throws IOException, XmlException {

        List<XWPFDocument> listOfDocuments = new ArrayList<XWPFDocument>();

        for (int i = 0; i < rooms.size(); i++) {
            DocxTemplate templateGenerator;


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
                    case "c":
                        return room.getRoomPin();
                    default:
                        throw new UnsupportedOperationException("Template contains invalid placeholder: " + templatePlaceholder);
                }
            };

            Function<Room, byte[]> qrGenerator = room -> {
                // TODO: This is a hack to integrate the PIN code into the QR Code but not in the hyperlink.
                val qrUri = UriComponentsBuilder.fromUri(uriConverter.apply(room).toUri())
                        .queryParam("pin", room.getRoomPin())
                        .build();
                return getQRCodePNGImage(qrUri, 500, 500);
            };

            if (privileged) {
                templateGenerator = new DocxTemplate<>(privilegedDocxTemplatePath.toFile(), textReplacer, qrGenerator);
            } else {
                templateGenerator = new DocxTemplate<>(defaultDocxTemplatePath.toFile(), textReplacer, qrGenerator);
            }
            listOfDocuments.add(templateGenerator.generate(rooms.get(i)));

        }
        return listOfDocuments;
    }
}
