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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.hs_mannheim.informatik.ct.controller.exception.InvalidRoomPinException;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.RoomVisit.Data;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/r")
@Slf4j
public class RoomController {
    @Autowired
    private RoomService roomService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private RoomVisitService roomVisitService;

    @Value("${allow_full_room_checkIn:false}")
    private boolean allowFullRoomCheckIn;

    @Value("${warning_for_full_room:true}")
    private boolean warningForFullRoom;

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
            Model model) throws InvalidRoomPinException {

        if (!allowFullRoomCheckIn) {
            overrideFullRoom = false;
        }

        // get room by room id
        if ("noId".equals(roomId) && roomIdFromRequest.isPresent())
            roomId = roomIdFromRequest.get();
        val room = roomService.getRoomOrThrow(roomId);

        // check room pin
        String roomPin = "";
        Boolean roomPinSet = true;
        if (roomPinFromRequest.isPresent() && !roomPinFromRequest.get().isEmpty()){
            roomPin = roomPinFromRequest.get();
            if (!(roomPin.equals(room.getRoomPin()))) {
                throw new InvalidRoomPinException();
            }
        } else {
            roomPinSet = false;
        }

        Room.Data roomData = new Room.Data(room);
        model.addAttribute("room", room);
        model.addAttribute("visitorCount", roomVisitService.getVisitorCount(room));
        model.addAttribute("roomData", roomData);
        model.addAttribute("visitData", new RoomVisit.Data(roomData));
        model.addAttribute("privileged", privileged);
        model.addAttribute("roomPin", roomPin);
        model.addAttribute("checkInOverwrite", overrideFullRoom);
        model.addAttribute("roomPinSet", roomPinSet);
        return "rooms/checkIn";
    }

    @PostMapping("/checkIn")
    @Transactional
    public String checkIn(@ModelAttribute RoomVisit.Data visitData, Model model) throws UnsupportedEncodingException, InvalidRoomPinException, InvalidEmailException, InvalidExternalUserdataException {
        isRoomPinValidOrThrow(visitData);

        val room = roomService.getRoomOrThrow(visitData.getRoomId());
        val visitorEmail = visitData.getVisitorEmail();
        val visitor = getOrCreateVisitorOrThrow(visitorEmail, visitData.getName(), visitData.getNumber(), visitData.getAddress());

        isRoomPinEqualOrThrow(visitData.getRoomPin(), room.getRoomPin());

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

        if (visitData.isPrivileged()) {
            val encodedVisitorEmail = URLEncoder.encode(visitorEmail, "UTF-8");
            return "redirect:/r/" + room.getId() + "/event-manager-portal?visitorEmail=" + encodedVisitorEmail;
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

    private void isRoomPinEqualOrThrow(String sendRoomPin, String actualRoomPin) throws InvalidRoomPinException {
        if (!sendRoomPin.equals(actualRoomPin))
            throw new InvalidRoomPinException();
    }

    private void isRoomPinValidOrThrow(Data visitData) throws InvalidRoomPinException {
        val roomPin = visitData.getRoomPin();
        if(roomPin==null || roomPin.isEmpty()) throw new InvalidRoomPinException();
        try {
            Long.parseLong(roomPin);
        } catch (NumberFormatException err) {
            throw new InvalidRoomPinException();
        }
    }

    /**
     * Check into a room even though it is full
     */
    @PostMapping("/checkInOverride")
    @Transactional
    public String checkInWithOverride(@ModelAttribute RoomVisit.Data visitData, Model model) throws
    UnsupportedEncodingException, InvalidEmailException, InvalidExternalUserdataException, InvalidRoomPinException {

        if (!allowFullRoomCheckIn) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Checking into a full room is not allowed");
        }

        isRoomPinValidOrThrow(visitData);

        val visitorEmail = visitData.getVisitorEmail();
        val room = roomService.getRoomOrThrow(visitData.getRoomId());
        val visitor = getOrCreateVisitorOrThrow(visitorEmail, visitData.getName(), visitData.getNumber(), visitData.getAddress());

        roomVisitService.checkOutVisitor(visitor);

        val visit = roomVisitService.visitRoom(visitor, room);

        if (visitData.isPrivileged()) {
            val encodedVisitorEmail = URLEncoder.encode(visitorEmail, "UTF-8");
            return "redirect:/r/" + room.getId() + "/event-manager-portal?visitorEmail=" + encodedVisitorEmail;
        }

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
        val room = roomService.getRoomOrThrow(roomId);

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
        val room = roomService.getRoomOrThrow(roomId);

        Room.Data roomData = new Room.Data(room);
        model.addAttribute("roomData", roomData);
        return "rooms/roomReset";
    }

    @GetMapping("/{roomId}/event-manager-portal")
    public String eventManagerPortal(
            @PathVariable String roomId,
            @RequestParam(required = true, value = "visitorEmail") String encodedVisitorEmail,
            Model model) throws UnsupportedEncodingException {

        val visitorEmail = URLDecoder.decode(encodedVisitorEmail, "UTF-8");
        val room = roomService.getRoomOrThrow(roomId);
        val currentRoomVisitorCount = roomVisitService.getVisitorCount(room);
        val isRoomOvercrowded = room.getMaxCapacity() <= currentRoomVisitorCount;
        
        // why is this here?
        //        val redirectURI = URLEncoder.encode("/r/" + roomId + "/event-manager-portal?visitorEmail=" + encodedVisitorEmail, "UTF-8");

        val roomData = new Room.Data(room);
        model.addAttribute("roomData", roomData);
        model.addAttribute("currentRoomVisitorCount", currentRoomVisitorCount);
        model.addAttribute("isRoomOvercrowded", isRoomOvercrowded);
        model.addAttribute("visitorEmail", visitorEmail);

        return "rooms/veranstaltungsleitenden-portal";
    }

    @PostMapping("/{roomId}/executeRoomReset")
    public String executeRoomReset(
            @PathVariable String roomId, Model model,
            @RequestParam(required = false, value = "redirectURI") Optional<String> redirectURIRequest) throws UnsupportedEncodingException {

        val room = roomService.getRoomOrThrow(roomId);

        String redirectURI = "/r/" + roomId + "?&privileged=true";
        if (redirectURIRequest.isPresent())
            redirectURI = URLDecoder.decode(redirectURIRequest.get(), "UTF-8");

        roomVisitService.resetRoom(room);

        return "redirect:" + redirectURI;
    }

    @PostMapping(value = "/{roomId}/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody RestResponse roomReset(
            @PathVariable String roomId,
            @RequestParam(required = true, value = "roomPin") Optional<String> roomPinRequested,
            Model model) {

        try {
            if (!roomPinRequested.isPresent()) throw new Exception("roomPin not found");
            val roomPin = roomPinRequested.get();
            val room = roomService.getRoomOrThrow(roomId);
            if(!room.getRoomPin().equals(roomPin)) throw new Exception("roomPin invalid");
            roomVisitService.resetRoom(room);

            return new RestResponse(true);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public static class RestResponse {
        public String message;
        public boolean success;

        public RestResponse(boolean success) {
            this.success = success;
        }

        public RestResponse(boolean success, String message) {
            this(success);
            this.message = message;
        }
    }

    @RequestMapping("/roomFull/{roomId}")
    public String roomFull(@PathVariable String roomId, Model model) {
        val room = roomService.getRoomOrThrow(roomId);

        Room.Data roomData = new Room.Data(room);
        model.addAttribute("roomData", roomData);

        if ((!warningForFullRoom && allowFullRoomCheckIn)) {
            log.info("room {} reached capacity, but currently no limitations apply.", roomData.getRoomName());
            return "redirect:/r/" + roomId +"?override=true&pin=" + roomData.getRoomPin();
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
    private Visitor getOrCreateVisitorOrThrow(String email, String name, String number, String address) throws
                                                            InvalidEmailException, InvalidExternalUserdataException {
        
        return visitorService.findOrCreateVisitor(email, name, number, address);
    }
    
}
