package de.hs_mannheim.informatik.ct.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hs_mannheim.informatik.ct.controller.exception.InvalidRoomPinException;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import lombok.val;

@RestController
@RequestMapping("/api")
public class RestRoomController {   // couldn't resist using this name ;-)
    @Autowired
    private RoomVisitService rvs;
    
    @Autowired
    private RoomService roomService;
    
    @RequestMapping(value="/rooms/{roomId}/visitors", method=RequestMethod.GET)
    public String getNumberOfCheckedInVisitors(@PathVariable(value = "roomId") String id, @RequestParam(value = "pin") String pin) throws InvalidRoomPinException {
       
        val room = roomService.getRoomOrThrow(id);
        
        if (pin == null || !room.getRoomPin().equals(pin))
            throw new InvalidRoomPinException();
        
        return "" + rvs.getVisitorCount(room);
    }
    
}