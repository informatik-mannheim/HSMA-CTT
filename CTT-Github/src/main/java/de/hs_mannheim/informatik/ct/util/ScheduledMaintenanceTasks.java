package de.hs_mannheim.informatik.ct.util;

import java.time.LocalTime;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsBesuchRepository;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;


/**
 * Schedules maintenance queries such as signing out visitors at the end of the day and deleting expired personal data
 */
@Component
public class ScheduledMaintenanceTasks {
    @Autowired
    private RoomVisitService roomVisitService;
    
	@Autowired
	private VeranstaltungsBesuchRepository repoVB;
    
 //   @Scheduled(fixedRate = 30 * 60 * 1000) // Every 30 minutes
	@Scheduled(cron = "0 55 3 * * *")	// 3:55 AM
    public void doMaintenance() {
        signOutAllVisitors(LocalTime.parse("21:00:00"));
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
    
	@Scheduled(cron = "0 55 2 * * *")	// 2:55 AM
	public void loescheAlteBesuche() {
		repoVB.loescheAlteBesuche();
	}
}
