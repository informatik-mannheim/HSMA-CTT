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
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;
import de.hs_mannheim.informatik.ct.util.DocxTemplate;
import lombok.val;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;



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

    public void writeRoomsPrintOutDocx(List<Room> rooms, OutputStream outputStream, Function<Room, UriComponents> uriConverter) throws IOException, InvalidFormatException {
        try(val document = getRoomsPrintOutDox(rooms, uriConverter)) {
           document.write(outputStream);
        }
    }

    @Deprecated
    public void writeContactList(Collection<VeranstaltungsBesuchDTO> contacts, String targetEmail, OutputStream outputStream) throws IOException {
        try(val workbook = utilities.excelErzeugen(contacts, targetEmail)) {
            workbook.write(outputStream);
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

        return templateGenerator.generate(rooms);
    }
}
