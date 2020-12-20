package de.hs_mannheim.informatik.ct.controller;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

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
    public String checkIn(@PathVariable String roomId, Model model) {
        Optional<Room> room = roomService.findByName(roomId);
        if (!room.isPresent()) {
            throw new RoomNotFoundException();
        }

        RoomVisit.Data visitData = new RoomVisit.Data();
        visitData.setRoomId(room.get().getName());
        model.addAttribute("visitData", visitData);
        return "rooms/checkIn";
    }

    @PostMapping("/checkIn")
    public String checkIn(@ModelAttribute RoomVisit.Data visitData, Model model) {
        Optional<Room> room = roomService.findByName(visitData.getRoomId());
        Besucher visitor = visitorService.findOrCreateVisitor(visitData.getVisitorEmail());

        List<RoomVisit> notCheckedOutVisits = roomVisitService.checkOutVisitor(visitor);

        String autoCheckoutValue = null;
        if (notCheckedOutVisits.size() != 0) {
            if (notCheckedOutVisits.size() > 1) {
                // TODO: Logging: Log a warning because a visitor was checked into multiple rooms at once.
            }

            autoCheckoutValue = notCheckedOutVisits.get(0).getRoom().getName();
        }

        model.addAttribute("autoCheckout", autoCheckoutValue);

        if (room.isPresent()) {
            RoomVisit visit = roomVisitService.visitRoom(visitor, room.get());

            visitData = new RoomVisit.Data(visit);
            model.addAttribute("visitData", visitData);

            return "rooms/roomVisitCheckedIn";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/checkOut")
    public String checkOut(@ModelAttribute RoomVisit.Data visitData) {
        Optional<Besucher> visitor = visitorService.findVisitorByEmail(visitData.getVisitorEmail());

        if (!visitor.isPresent()) {
            throw new VisitorNotFoundException();
        }

        roomVisitService.checkOutVisitor(visitor.get());

        return "redirect:/";
    }

    @GetMapping("/import")
    public String roomImport() {
        return "rooms/roomImport";
    }

    @PostMapping("/import")
    public String roomTableImport(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            roomService.ImportFromCSV(
                    new BufferedReader(
                            new InputStreamReader(is, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "rooms/importCompleted";
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Room not found")
    public static class RoomNotFoundException extends RuntimeException {
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Visitor not found")
    public static class VisitorNotFoundException extends RuntimeException {
    }
}