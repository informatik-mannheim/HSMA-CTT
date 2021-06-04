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

@Service
public class DynamicContentService {
    @Autowired
    private Utilities utilities;

    private final Path docxTemplatePath = FileSystems.getDefault().getPath("templates/printout/room-printout.docx");

    public byte[] getQRCodePNGImage(UriComponents uri, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCode.from(uri.toUriString())
                .withSize(width, height)
                .to(ImageType.PNG)
                .writeTo(out);

        return out.toByteArray();
    }

    public void writeRoomsPrintOutDocx(List<Room> rooms, OutputStream outputStream, Function<Room, UriComponents> uriConverter) throws IOException, XmlException {
        List<XWPFDocument> ListOfDocuments = getRoomsPrintOutDox(rooms, uriConverter);

        for (XWPFDocument document: ListOfDocuments){
            try  {
                System.out.println("Alle Räume: " + rooms);
                System.out.println("Wir haben soeben ein neues Dokument erstellt " + document);
                document.write(outputStream);
            } catch (IOException e){
System.err.println("Das gar nicht so gut derr Error hier" + e);
            }
        }


    }

    public void writeContactList(Collection<Contact<?>> contacts, Visitor target, ContactListGenerator generator, OutputStream outputStream) throws IOException {
        try (val workbook = generator.generateWorkbook(contacts, target.getEmail())) {
            workbook.write(outputStream);
        }
    }

    private List<XWPFDocument> getRoomsPrintOutDox(List<Room> rooms, Function<Room, UriComponents> uriConverter) throws IOException, XmlException {

        List<XWPFDocument> listOfDocuments = new ArrayList<XWPFDocument>();

        for (int i = 0; i < rooms.size(); i++) {
            System.out.println("Wir erzeugen ein neues File juhuu");


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

            val templateGenerator = new DocxTemplate<>(docxTemplatePath.toFile(), textReplacer, qrGenerator);


        templateGenerator.generate(rooms.get(i)).write(new FileOutputStream(new File("templates/printout/WriteInGetRooms"+i+".docx")));

            listOfDocuments.add(templateGenerator.generate(rooms.get(i)));

        }
        return listOfDocuments;
    }


}
