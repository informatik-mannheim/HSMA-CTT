package de.hs_mannheim.informatik.ct.controller;

import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.LocalTime;
import java.time.Period;


/**
 * Schedules maintenance queries such as signing out visitors at the end of the day and deleting expired personal data
 */
@Controller
public class ScheduledMaintenanceController {
    @Autowired
    private RoomVisitService roomVisitService;
    @Scheduled(fixedRate = 30 * 60 * 1000) // Every 30 minutes

    public void doMaintenance() {
        signOutAllVisitors(LocalTime.parse("18:00:00"));
        deleteExpiredVisitRecords(Period.ofWeeks(4));
    }

    public void signOutAllVisitors(LocalTime forcedEndTime) {
        if(LocalTime.now().isAfter(forcedEndTime)) {
            roomVisitService.checkOutAllVisitors(forcedEndTime);
        }
    }

    public void deleteExpiredVisitRecords(Period recordLifeTime) {
        roomVisitService.deleteExpiredRecords(recordLifeTime);
    }
}
