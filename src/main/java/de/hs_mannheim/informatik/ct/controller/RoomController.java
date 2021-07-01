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

import de.hs_mannheim.informatik.ct.controller.exception.InvalidRoomPinException;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


@Controller
@RequestMapping("/r")
public class RoomController {
    Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private RoomVisitService roomVisitService;

    @Value("${allow_full_room_checkIn:false}")
    private boolean allowFullRoomCheckIn;

    /**
     * Shows the check-in form page for the given room.
     *
     * @param roomId            The room id of the room.
     * @param roomIdFromRequest The room id as a request param for the search function. RoomId has to be set to 'noId'.
     * @param overrideFullRoom  Shows the form even if the room is full.
     * @param model             The Spring model param.
     * @return A spring template string.
     */
    // TODO: Can we handle rooms with non ASCII names?
    @GetMapping("/{roomId}")
    public String checkIn(@PathVariable String roomId,
                          @RequestParam(required = false, value = "roomId") Optional<String> roomIdFromRequest,
                          @RequestParam(required = false, defaultValue = "false") Boolean privileged,
                          @RequestParam(required = false, value = "pin") Optional<String> roomPinFromRequest,
                          @RequestParam(required = false, value = "override", defaultValue = "false") boolean overrideFullRoom,
                          Model model) {
        if (!allowFullRoomCheckIn) {
            overrideFullRoom = false;
        }

        // get roomId from form on landing page (index.html)
        if ("noId".equals(roomId) && roomIdFromRequest.isPresent())
            roomId = roomIdFromRequest.get();

        String roomPin = "";
        if (roomPinFromRequest.isPresent())
            roomPin = roomPinFromRequest.get();

        val room = getRoomOrThrow(roomId);

        if (!overrideFullRoom && roomVisitService.isRoomFull(room)) {
            return "forward:roomFull/" + room.getId();
        }

        Room.Data roomData = new Room.Data(room);
        model.addAttribute("room", room);
        model.addAttribute("visitorCount", roomVisitService.getVisitorCount(room));
        model.addAttribute("roomData", roomData);
        model.addAttribute("visitData", new RoomVisit.Data(roomData));
        model.addAttribute("privileged", privileged);
        model.addAttribute("roomPin", roomPin);
        model.addAttribute("checkInOverwrite", overrideFullRoom);

        return "rooms/checkIn";
    }

    @PostMapping("/checkIn")
    @Transactional
    public String checkIn(
            @ModelAttribute RoomVisit.Data visitData,
            Model model
    ) throws InvalidRoomPinException {
        val room = getRoomOrThrow(visitData.getRoomId());

        if(!visitData.getRoomPin().equals(room.getRoomPin()))
            throw new InvalidRoomPinException();

        val visitor = getOrCreateVisitorOrThrow(visitData.getVisitorEmail());

        val notCheckedOutVisits = roomVisitService.checkOutVisitor(visitor);

        String autoCheckoutValue = null;
        if (notCheckedOutVisits.size() != 0) {
            val checkedOutRoom = notCheckedOutVisits.get(0).getRoom();
            autoCheckoutValue = checkedOutRoom.getName();

            // If the user is automatically checked out of the same room they're trying to
            // check into, show them the checked out page instead (Auto checkout after scanning room qr code twice)
            if (room.getId().equals(checkedOutRoom.getId())) {
                return "forward:checkedOut/";
            }
        }

        if (roomVisitService.isRoomFull(room)) {
            return "forward:roomFull/" + room.getId();
        }

        val visit = roomVisitService.visitRoom(visitor, room);
        val currentVisitCount = roomVisitService.getVisitorCount(room);

        visitData = new RoomVisit.Data(visit, currentVisitCount);
        model.addAttribute("visitData", visitData);

        return "rooms/checkedIn";
    }

    /**
     * Check into a room even though it is full
     */
    @PostMapping("/checkInOverride")
    @Transactional
    public String checkInWithOverride(@ModelAttribute RoomVisit.Data visitData, Model model) {
        if (!allowFullRoomCheckIn) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Checking into a full room is not allowed");
        }

        val room = getRoomOrThrow(visitData.getRoomId());
        val visitor = getOrCreateVisitorOrThrow(visitData.getVisitorEmail());

        roomVisitService.checkOutVisitor(visitor);

        val visit = roomVisitService.visitRoom(visitor, room);
        val currentVisitCount = roomVisitService.getVisitorCount(room);

        visitData = new RoomVisit.Data(visit, currentVisitCount);
        model.addAttribute("visitData", visitData);

        return "rooms/checkedIn";
    }

    @PostMapping("/checkOut")
    public String checkOut(@ModelAttribute RoomVisit.Data visitData) {
        val visitor = getVisitorOrThrow(visitData.getVisitorEmail());

        roomVisitService.checkOutVisitor(visitor);
        return "redirect:/r/checkedOut";
    }

    @GetMapping("/{roomId}/checkOut")
    public String checkoutPage(@PathVariable String roomId, Model model) {
        val room = getRoomOrThrow(roomId);

        Room.Data roomData = new Room.Data(room);
        model.addAttribute("room", room);
        model.addAttribute("checkout", true);
        model.addAttribute("roomData", roomData);
        model.addAttribute("visitData", new RoomVisit.Data(roomData));
        model.addAttribute("privileged", false);

        return "rooms/checkIn";
    }

    @GetMapping("/{roomId}/roomReset")
    public String roomReset(@PathVariable String roomId, Model model) {
        val room = getRoomOrThrow(roomId);

        Room.Data roomData = new Room.Data(room);
        model.addAttribute("roomData", roomData);
        return "rooms/roomReset";
    }

    @GetMapping("/{roomId}/event-manager-portal")
    public String eventManagerPortal(
            @PathVariable String roomId,
            @RequestParam(required = true, value = "visitorEmail") String encodedVisitorEmail,
            Model model
    )
    throws UnsupportedEncodingException {

        val visitorEmail = URLDecoder.decode(encodedVisitorEmail, "UTF-8");
        val room = getRoomOrThrow(roomId);
        val currentRoomVisitorCount = roomVisitService.getVisitorCount(room);
        val isRoomOvercrowded = room.getMaxCapacity()<=currentRoomVisitorCount;
        val redirectURI = URLEncoder.encode("/r/"+roomId+"/event-manager-portal?visitorEmail="+encodedVisitorEmail, "UTF-8");
        val roomResetEndpoint = "/r/"+roomId+"/executeRoomReset?redirectURI="+redirectURI;

        val roomData = new Room.Data(room);
        model.addAttribute("roomData", roomData);
        model.addAttribute("currentRoomVisitorCount", currentRoomVisitorCount);
        model.addAttribute("isRoomOvercrowded", isRoomOvercrowded);
        model.addAttribute("roomResetEndpoint", roomResetEndpoint);
        model.addAttribute("redirectURI", redirectURI);
        model.addAttribute("visitorEmail", visitorEmail);

        return "rooms/veranstaltungsleitenden-portal";
    }

    @PostMapping("/{roomId}/executeRoomReset")
    public String executeRoomReset(
            @PathVariable String roomId, Model model,
            @RequestParam(required = false, value = "redirectURI") Optional<String> redirectURIRequest
                                   ) throws UnsupportedEncodingException {
        val room = getRoomOrThrow(roomId);

        String redirectURI = "/r/" + roomId + "?&privileged=true";
        if(redirectURIRequest.isPresent())
            redirectURI = URLDecoder.decode(redirectURIRequest.get(), "UTF-8");

        roomVisitService.resetRoom(room);

        return "redirect:"+redirectURI;
    }

    @RequestMapping("/roomFull/{roomId}")
    public String roomFull(@PathVariable String roomId, Model model) {
        val room = getRoomOrThrow(roomId);

        int visitorCount = roomVisitService.getVisitorCount(room);
        int maxCapacity = room.getMaxCapacity();
        Room.Data roomData = new Room.Data(room);
        model.addAttribute("roomData", roomData);
        if (visitorCount < maxCapacity) {
            model.addAttribute("visitData", new RoomVisit.Data(roomData));
            return "redirect:/r/" + roomId;
        } else {
            return "rooms/full";
        }
    }

    @RequestMapping("/checkedOut")
    public String checkedOutPage() {
        return "rooms/checkedOut";
    }

    @GetMapping("/import")
    public String roomImport() {
        return "rooms/roomImport";
    }

    @PostMapping("/import")
    public String roomTableImport(@RequestParam("file") MultipartFile file, Model model) {
        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.indexOf("."));

        try (InputStream is = file.getInputStream()) {
            if (extension.equals(".csv"))
                roomService.importFromCsv(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
            else if (extension.equals(".xlsm"))
                roomService.importFromExcel(is);
            else
                throw new InvalidFileUploadException();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "rooms/importCompleted";
    }

    public static String getRoomCheckinPath(Room room) {
        return "r/" + room.getId();
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Room not found")
    public static class RoomNotFoundException extends RuntimeException {
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Visitor not found")
    public static class VisitorNotFoundException extends RuntimeException {
    }

    @ResponseStatus(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE, reason = "Not a supported filetype, only .csv or .xlsm will work!")
    public static class InvalidFileUploadException extends RuntimeException {
    }

    /**
     * Gets a visitor by email or throws a VisitorNotFoundException if a visitor with that email doesn't exist (yet).
     *
     * @param email The visitors email.
     * @return The visitor.
     */
    private Visitor getVisitorOrThrow(String email) {
        Optional<Visitor> visitor = visitorService.findVisitorByEmail(email);

        if (!visitor.isPresent()) {
            throw new VisitorNotFoundException();
        }

        return visitor.get();
    }

    /**
     * Gets an existing visitor by email or creates a new one. Throws a 'bad request' ResponseStatusException if an InvalidEmailException is thrown.
     *
     * @param email The visitors email.
     * @return The visitor.
     */
    private Visitor getOrCreateVisitorOrThrow(String email) {
        try {
            return visitorService.findOrCreateVisitor(email);
        } catch (InvalidEmailException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Email");
        }
    }

    /**
     * Gets a room by id or throws a RoomNotFoundException.
     *
     * @param roomId The rooms id.
     * @return The room.
     */
    private Room getRoomOrThrow(String roomId) {
        Optional<Room> room = roomService.findByName(roomId);
        if (room.isPresent()) {
            return room.get();
        } else {
            throw new RoomNotFoundException();
        }
    }
}
