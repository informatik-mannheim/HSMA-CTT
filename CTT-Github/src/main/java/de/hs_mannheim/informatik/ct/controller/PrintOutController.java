package de.hs_mannheim.informatik.ct.controller;

import de.hs_mannheim.informatik.ct.persistence.services.BuildingService;
import de.hs_mannheim.informatik.ct.persistence.services.DynamicContentService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("printout")
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

    @GetMapping(value = "/rooms/{building}")
    public ResponseEntity<StreamingResponseBody> getRoomPrintout(
            @PathVariable(value = "building") String building,
            HttpServletRequest request) {
        val roomsInBuilding = buildingService.getAllRoomsInBuilding(building);
        if (roomsInBuilding.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found or empty");
        }

        val outFileName = String.format("Gebäude %s.docx", building);

        StreamingResponseBody responseBody = outputStream -> {
            try {
                contentService.writeRoomsPrintOutDocx(
                        roomsInBuilding,
                        outputStream,
                        room -> utilities.getUriToLocalPath(
                                RoomController.getRoomCheckinPath(room),
                                request
                        )
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + outFileName)
                .body(responseBody);

    }
}
