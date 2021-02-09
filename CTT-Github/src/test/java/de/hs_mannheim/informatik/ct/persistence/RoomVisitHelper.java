package de.hs_mannheim.informatik.ct.persistence;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.Date;

public class RoomVisitHelper {
    private static long fakeIdCounter = 0;
    private static final Room room = new Room("autoCheckOutTest", 20);

    public static RoomVisit generateVisit(Visitor visitor, LocalDateTime start, LocalDateTime end) {
        Date endDate = null;
        if (end != null) {
            endDate = TimeUtil.convertToDate(end);
        }

        return new RoomVisit(
                room,
                fakeIdCounter++,
                TimeUtil.convertToDate(start),
                endDate,
                visitor
        );
    }
}
