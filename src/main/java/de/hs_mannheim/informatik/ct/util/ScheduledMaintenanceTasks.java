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

package de.hs_mannheim.informatik.ct.util;

import java.time.LocalTime;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.hs_mannheim.informatik.ct.persistence.services.EventVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import lombok.extern.slf4j.Slf4j;

/**
 * Schedules maintenance queries such as signing out visitors at the end of the day and deleting expired personal data
 */
@Component
@Slf4j
public class ScheduledMaintenanceTasks {
    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private EventVisitService eventVisitService;

    private final int CRON_HOUR = 3;
    private final int CRON_MINUTE = 55;
    private final String FORCED_END_TIME = "00:00:00";

    //@Scheduled(fixedRate = 5 * 60 * 1000) // Every 5 Minutes
    @Scheduled(cron = "0 " + CRON_MINUTE + " " + CRON_HOUR + " * * *")    // 3:55 AM
    public void doMaintenance() {
        log.info("Auto-Checkout and deletion of old records triggered.");
        
        signOutAllVisitors(LocalTime.parse(FORCED_END_TIME));
        deleteExpiredVisitRecords(Period.ofWeeks(4));
    }

    public void signOutAllVisitors(LocalTime forcedEndTime) {
        roomVisitService.checkOutAllVisitors(forcedEndTime);
    }

    public void deleteExpiredVisitRecords(Period recordLifeTime) {
        eventVisitService.deleteExpiredRecords(recordLifeTime);
        roomVisitService.deleteExpiredRecords(recordLifeTime);
    }
    
}
