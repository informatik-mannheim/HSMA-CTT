package de.hs_mannheim.informatik.ct.controller;

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

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.BuildingService;
import de.hs_mannheim.informatik.ct.persistence.services.DynamicContentService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;


@Controller
@RequestMapping("/printout")
public class PrintOutController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private BuildingService buildingService;


    @Autowired
    private DynamicContentService contentService;

    @Autowired
    private Utilities utilities;

    @Value("${server.port}")
    private String port;

    @Value("${hostname}")
    private String host;

    @GetMapping(value = "/rooms")
    public String getRoomPrintoutList(Model model) {
        return "rooms/roomPrintout";
    }


    @RequestMapping(value = "/rooms/download")
    public ResponseEntity<StreamingResponseBody> getRoomPrintout(
            HttpServletRequest request,
            @RequestParam(value = "privileged") boolean privileged) {
        val allRooms = buildingService.getAllRooms();

        StreamingResponseBody responseBody = outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (Room room : allRooms) {
                    contentService.writeRoomPrintOutDocx(
                            room,
                            privileged,
                            zos,
                            uriToPath -> utilities.getUriToLocalPath(
                                    RoomController.getRoomCheckinPath(room),
                                    request
                            )
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };


        if (privileged) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"PrivilegedRoomNotes.zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(responseBody);
        } else {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"RoomNotes.zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(responseBody);
        }
    }

}
