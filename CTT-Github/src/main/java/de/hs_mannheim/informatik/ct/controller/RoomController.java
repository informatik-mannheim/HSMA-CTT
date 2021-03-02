package de.hs_mannheim.informatik.ct.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import lombok.val;

@Controller
@RequestMapping("r")
public class RoomController {
    @Autowired
    private RoomService roomService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private RoomVisitService roomVisitService;

    // TODO: Can we handle rooms with non ASCII names?
    @GetMapping("/{roomId}")
    public String checkIn(@PathVariable String roomId, @RequestParam(required = false, value = "roomId") Optional<String> roomIdFromRequest, Model model) {
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
        model.addAttribute("roomData", roomData);
        model.addAttribute("visitData", new RoomVisit.Data(roomData));

        return "rooms/checkIn";
    }

    @PostMapping("/checkIn")
    public String checkIn(@ModelAttribute RoomVisit.Data visitData, Model model) {
        Optional<Room> room = roomService.findByName(visitData.getRoomId());
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
                // TODO: Logging: Log a warning because a visitor was checked into multiple rooms at once.
            }

            val checkedOutRoom = notCheckedOutVisits.get(0).getRoom();
            autoCheckoutValue = checkedOutRoom.getName();

            // If the user is automatically checked out of the same room they're trying to check into,
            // show them the checked out page instead (Auto checkout after scanning room qr code twice)
            if (room.isPresent() &&
                    room.get().getId().equals(checkedOutRoom.getId())) {
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
        model.addAttribute("roomData", roomData);
        model.addAttribute("visitData", new RoomVisit.Data(roomData));

        // The check-in page can handle both check-in and checkout with a css toggle
        model.addAttribute("checkout", true);
        return "rooms/checkIn";
    }

    @RequestMapping("/roomFull/{roomId}")
    public String roomFull(@PathVariable String roomId, Model model) {
        Optional<Room> room = roomService.findByName(roomId);
        if (!room.isPresent()) {
            throw new RoomNotFoundException();
        }

        model.addAttribute("roomData", new Room.Data(room.get()));
        return "rooms/full";
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
    public String roomTableImport(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            roomService.importFromCsv(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
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
}
