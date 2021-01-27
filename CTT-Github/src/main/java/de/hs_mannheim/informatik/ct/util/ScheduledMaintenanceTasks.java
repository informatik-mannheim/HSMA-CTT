package de.hs_mannheim.informatik.ct.util;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;
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
    private RoomRepository roomRepository;
    
	@Autowired
	private VeranstaltungsBesuchRepository repoVB;
	
	@Value("${spring.datasource.driverClassName}")
	private String db;
    
 //   @Scheduled(fixedRate = 30 * 60 * 1000) // Every 30 minutes
	@Scheduled(cron = "0 55 3 * * *")	// 3:55 AM
    public void doMaintenance() {
        signOutAllVisitors(LocalTime.parse("21:00:00"));
        
        deleteExpiredVisitRecords(Period.ofWeeks(4));
        loescheAlteBesuche();
        
        doDbBackup();
    }

    public void signOutAllVisitors(LocalTime forcedEndTime) {
        if(LocalTime.now().isAfter(forcedEndTime)) {
            roomVisitService.checkOutAllVisitors(forcedEndTime);
        }
    }

    public void deleteExpiredVisitRecords(Period recordLifeTime) {
        roomVisitService.deleteExpiredRecords(recordLifeTime);
    }
    
	public void loescheAlteBesuche() {
		repoVB.loescheAlteBesuche();
	}
	
	public void doDbBackup() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String target = "bak/" + LocalDate.now().format(formatter)+ "-db-backup" + ".zip";

		if ("org.h2.Driver".contentEquals(db)) {
			roomRepository.backupH2DB(target);

			File[] files = new File("bak/").listFiles();

			for (File file : files) {
				long diff = new Date().getTime() - file.lastModified();

				if (diff > 7 * 24 * 60 * 60 * 1000) {		// delete after 7 days
					file.delete();
				}
			}
		} // H2DB

	}
}
