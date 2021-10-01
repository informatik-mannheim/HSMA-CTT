/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.xmlbeans.XmlException;
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

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.BuildingService;
import de.hs_mannheim.informatik.ct.persistence.services.DynamicContentService;
import lombok.val;

@Controller
@RequestMapping("/printout")
public class PrintOutController {

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
    private int threadCount = 4;

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
            try (val zos = new ZipOutputStream(outputStream)) {

                val listOfTheads = new ArrayList<Thread>();

                for (int i = 0; i < threadCount; i++) {
                    int counter = i;
                    Thread t = new Thread(() -> {
                        for (Room room : allRooms.subList(allRooms.size() * counter / threadCount, allRooms.size() * (counter + 1) / threadCount)) {
                            try {
                                contentService.addRoomPrintOutDocx(
                                        room,
                                        privileged,
                                        zos,
                                        uriToPath -> {
                                            val scheme = request.getScheme();
                                            val localPath = RoomController.getRoomCheckinPath(room);
                                            if(privileged){
                                                return utilities.getUriToLocalPath(scheme, localPath, "privileged=true");
                                            }else{
                                                return utilities.getUriToLocalPath(scheme, localPath);
                                            }
                                        }
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (XmlException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    );
                    listOfTheads.add(t);
                    t.start();
                }

                for (Thread thread : listOfTheads) {
                    thread.join();
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
