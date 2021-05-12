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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import de.hs_mannheim.informatik.ct.controller.exception.InvalidRoomPinException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import lombok.val;


@Controller
@RequestMapping("/r")
public class RoomController {
    @Autowired
    private RoomService roomService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private RoomVisitService roomVisitService;

    // TODO: Can we handle rooms with non ASCII names?
    @GetMapping("/{roomId}")
    public String checkIn(@PathVariable String roomId,
                          @RequestParam(required = false, value = "roomId") Optional<String> roomIdFromRequest,
                          @RequestParam(required = false, defaultValue = "false") Boolean privileged, Model model) {
        // get roomId from form on landing page (index.html)
        if ("noId".equals(roomId) && roomIdFromRequest.isPresent())
            roomId = roomIdFromRequest.get();

        Optional<Room> room = roomService.findByName(roomId);
        if (!room.isPresent()) {
            throw new RoomNotFoundException();
        }
        if (roomVisitService.isRoomFull(room.get())) {
            return "forward:roomFull/" + room.get().getId();
        }

        Room.Data roomData = new Room.Data(room.get());
        model.addAttribute("room", room.get());
        model.addAttribute("visitorCount", roomVisitService.getVisitorCount(room.get()));
        model.addAttribute("roomData", roomData);
        model.addAttribute("visitData", new RoomVisit.Data(roomData));
        model.addAttribute("privileged", privileged);

        return "rooms/checkIn";
    }

    @PostMapping("/checkIn")
    @Transactional
    public String checkIn(@ModelAttribute RoomVisit.Data visitData, Model model) {
        Optional<Room> room = roomService.findByName(visitData.getRoomId());

        try {
            if(!visitData.getRoomPin().equals(room.get().getRoomPin()))
                throw new InvalidRoomPinException();
        } catch(InvalidRoomPinException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Pin");
        }

        Visitor visitor = null;
        try {
            visitor = visitorService.findOrCreateVisitor(visitData.getVisitorEmail());
        } catch (InvalidEmailException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Email");
        }

        List<RoomVisit> notCheckedOutVisits = roomVisitService.checkOutVisitor(visitor);

        String autoCheckoutValue = null;
        if (notCheckedOutVisits.size() != 0) {
            if (notCheckedOutVisits.size() > 1) {
                // TODO: Logging: Log a warning because a visitor was checked into multiple
                // rooms at once.
            }

            val checkedOutRoom = notCheckedOutVisits.get(0).getRoom();
            autoCheckoutValue = checkedOutRoom.getName();

            // If the user is automatically checked out of the same room they're trying to
            // check into,
            // show them the checked out page instead (Auto checkout after scanning room qr
            // code twice)
            if (room.isPresent() && room.get().getId().equals(checkedOutRoom.getId())) {
                return "forward:checkedOut/";
            }
        }

        model.addAttribute("autoCheckout", autoCheckoutValue);

        if (room.isPresent()) {
            if (roomVisitService.isRoomFull(room.get())) {
                return "forward:roomFull/" + room.get().getId();
            }

            val visit = roomVisitService.visitRoom(visitor, room.get());
            val currentVisitCount = roomVisitService.getVisitorCount(room.get());

            visitData = new RoomVisit.Data(visit, currentVisitCount);
            model.addAttribute("visitData", visitData);

            return "rooms/checkedIn";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/checkOut")
    public String checkOut(@ModelAttribute RoomVisit.Data visitData) {
        Optional<Visitor> visitor = visitorService.findVisitorByEmail(visitData.getVisitorEmail());

        if (!visitor.isPresent()) {
            throw new VisitorNotFoundException();
        }
        roomVisitService.checkOutVisitor(visitor.get());
        return "redirect:/r/checkedOut";
    }

    @GetMapping("/{roomId}/checkOut")
    public String checkoutPage(@PathVariable String roomId, Model model) {
        Optional<Room> room = roomService.findByName(roomId);
        if (!room.isPresent()) {
            throw new RoomNotFoundException();
        }
        Room.Data roomData = new Room.Data(room.get());
        model.addAttribute("room", room.get());
        model.addAttribute("checkout", true);
        model.addAttribute("roomData", roomData);
        model.addAttribute("visitData", new RoomVisit.Data(roomData));
        model.addAttribute("privileged", false);
        return "rooms/checkIn";
    }

    @GetMapping("/{roomId}/roomReset")
    public String roomReset(@PathVariable String roomId, Model model) {
        Optional<Room> room = roomService.findByName(roomId);
        if (!room.isPresent()) {
            throw new RoomNotFoundException();
        }
        Room.Data roomData = new Room.Data(room.get());
        model.addAttribute("roomData", roomData);
        return "rooms/roomReset";
    }

    @PostMapping("/{roomId}/executeRoomReset")
    public String executeRoomReset(@PathVariable String roomId, Model model) {
        Optional<Room> room = roomService.findByName(roomId);

        roomVisitService.resetRoom(room.get());
        return "redirect:/r/" + roomId + "?&privileged=true";
    }

    @RequestMapping("/roomFull/{roomId}")
    public String roomFull(@PathVariable String roomId, Model model) {
        Optional<Room> room = roomService.findByName(roomId);
        if (!room.isPresent()) {
            throw new RoomNotFoundException();
        }
        Room requestedRoom = room.get();
        int visitorCount = roomVisitService.getVisitorCount(requestedRoom);
        int maxCapacity = requestedRoom.getMaxCapacity();
        Room.Data roomData = new Room.Data(room.get());
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
        String extension = fileName.substring(fileName.indexOf("."), fileName.length());

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
}
