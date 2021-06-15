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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.hibernate.engine.jdbc.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
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
        model.addAttribute("buildings", buildingService.getAllBuildings());
        return "rooms/roomPrintout";
    }


    private DeferredResult<ResponseEntity<byte[]>> asyncCallHelper(String building, List<Room> oneRoom, HttpServletRequest request){
        val outFileName = String.format("Gebäude %s.docx", building);
        val result = new DeferredResult<ResponseEntity<byte[]>>(120 * 1000L);
        CompletableFuture.runAsync(() -> {
            try (val buffer = new ByteArrayOutputStream()) {
                contentService.writeRoomsPrintOutDocx(
                        oneRoom,
                        buffer,
                        room -> utilities.getUriToLocalPath(
                                RoomController.getRoomCheckinPath(room),
                                request
                        ));
                val response = ResponseEntity
                        .ok()
                        .contentType(
                                MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + outFileName)
                        .body(buffer.toByteArray());
                result.setResult(response);
            } catch (IOException | XmlException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    @GetMapping(value = "/rooms/zip")
    public ZipOutputStream getRoomPrintout(
            HttpServletRequest request) throws IOException {



        FileOutputStream fos = new FileOutputStream("hello-world.zip");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);

        try {
            for (int i = 0; i < 10; i++) {
                // not available on BufferedOutputStream
                zos.putNextEntry(new ZipEntry("hello-world." + i + ".txt"));
                zos.write("Hello World!".getBytes());
                // not available on BufferedOutputStream
                zos.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            zos.close();
        }

        return zos;


//        System.out.println("Das hier ist die Request: " + request);
//
//        DeferredResult<ResponseEntity<byte[]>> test = null;
//
//        for (String allBuildings: buildingService.getAllBuildings()){
//            val roomsInBuilding = buildingService.getAllRoomsInBuilding(allBuildings);
//            test = asyncCallHelper(allBuildings, roomsInBuilding, request);
//            break;
//        }
//        return test;


    }

    @GetMapping(value = "rooms/zip-download", produces="application/zip")
    public void zipDownload(HttpServletResponse response) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        for (String building :getr  {

            getRoomsPrintOutDox
            String fileBasePath = null;
            FileSystemResource resource = new FileSystemResource(fileBasePath + fileName);
            ZipEntry zipEntry = new ZipEntry(resource.getFilename());
            zipEntry.setSize(resource.contentLength());
            zipOut.putNextEntry(zipEntry);
            StreamUtils.copy(resource.getInputStream(), zipOut);
            zipOut.closeEntry();
        }
        zipOut.finish();
        zipOut.close();
        response.setStatus(HttpServletResponse.SC_OK);
        String zipFileName = "itDoBeWorking";
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");
    }
}
