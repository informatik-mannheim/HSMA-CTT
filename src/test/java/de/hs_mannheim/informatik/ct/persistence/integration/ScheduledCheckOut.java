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

package de.hs_mannheim.informatik.ct.persistence.integration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import de.hs_mannheim.informatik.ct.model.CheckOutSource;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.EventVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.util.ScheduledMaintenanceTasks;
import de.hs_mannheim.informatik.ct.util.TimeUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.mockito.InjectMocks;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class ScheduledCheckOut {
    @TestConfiguration
    static class RoomVisitServiceTestContextConfig {
        @Bean
        public RoomVisitService service() {
            return new RoomVisitService();
        }

        @Bean
        public EventVisitService eventVisitService() {
            return new EventVisitService();
        }

        @Bean
        public ScheduledMaintenanceTasks scheduledMaintenanceTasks(){
            return new ScheduledMaintenanceTasks();
        }
    }

    @MockBean
    public DateTimeService dateTimeService;

    @InjectMocks
    private RoomVisitService roomVisitService;

    @Autowired
    private ScheduledMaintenanceTasks scheduledMaintenanceTasks;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomVisitRepository roomVisitRepository;

    private LocalDateTime now = LocalDateTime.now();

    // copied from ScheduledMaintenanceTasks
    // WARNING needs to be changed if constants in ScheduledMaintenanceTasks change
    private int maintenanceHour = 3;
    private int maintenanceMinute = 55;
    private String forcedEndTime = "00:00:00";

    @BeforeEach
    void mockDateTimeService(){
        // this changes the actual time to 3:55 for roomVisitService. This is the time the cron job should run
        when(dateTimeService.getNow()).thenReturn(LocalDateTime.now().withHour(maintenanceHour).withMinute(maintenanceMinute));
    }

    @Test
    void signOutAllVisitors_morning() {
        // start of the day
        signOutAllVisitorsToSpecificTime(LocalTime.parse("00:00:00"));
    }

    @Test
    void signOutAllVisitors_lastLecture() {
        // after last lecture
        signOutAllVisitorsToSpecificTime(LocalTime.parse("19:00:00"));
    }

    @Test
    void signOutAllVisitors_endOfWork() {
        // end of workday
        signOutAllVisitorsToSpecificTime(LocalTime.parse("21:00:00"));
    }

    @Test
    void signOutAllVisitors_scheduledTime(){
        // this tests the actual forced end time
        signOutAllVisitorsToSpecificTime(LocalTime.parse(forcedEndTime));
    }

    /**
     * tests daily check out by generating visits at important times and running doMaintenance()
     */
    @Test
    void scheduledMaintenanceTasksTest() {
        Room room = entityManager.persist(new Room("A001", "A", 10));
        // visitors that checked in today and should not get checked out by maintenance task
        List<RoomVisit> visitsToday = Arrays.asList(
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("1")),
                        room,
                        TimeUtil.convertToDate(now.withHour(3).withMinute(54).withSecond(59))
                )),
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("2")),
                        room,
                        TimeUtil.convertToDate(now.withHour(0).withMinute(0).withSecond(0))
                )),
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("3")),
                        room,
                        TimeUtil.convertToDate(now.withHour(12).withMinute(0).withSecond(0))
                ))
        );
        // visitors that checked in yesterday and should get checked out by maintenance task
        List<RoomVisit> visitsYesterday = Arrays.asList(
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("4")),
                        room,
                        TimeUtil.convertToDate(now.withHour(23).withMinute(59).withSecond(59).minusDays(1))
                )),
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("5")),
                        room,
                        TimeUtil.convertToDate(now.withHour(0).withMinute(0).withSecond(1).minusDays(1))
                ))
        );

        roomVisitRepository.saveAll(visitsToday);
        roomVisitRepository.saveAll(visitsYesterday);

        // run check out task
        scheduledMaintenanceTasks.doMaintenance();

        // tests repository
        assertThat(roomVisitRepository.findNotCheckedOutVisits(room),
                everyItem(in(visitsToday)));
        assertThat(roomVisitRepository.findNotCheckedOutVisits(room),
                not(everyItem(in(visitsYesterday))));

        // test RoomVisit objects
        visitsToday.stream().allMatch(
                visit -> visit.getCheckOutSource().equals(CheckOutSource.NotCheckedOut) && visit.getEndDate() == null
        );

        visitsYesterday.stream().allMatch(
                visit -> visit.getCheckOutSource().equals(CheckOutSource.AutomaticCheckout) && visit.getEndDate() != null
        );
    }

    /**
     * runs singOutAllVisitors() to a specified forcedEndTime and test if the visitor got checked out properly.
     * @param forcedEndtime every visit that happened before this time is going to be checked out
     */
    private void signOutAllVisitorsToSpecificTime(LocalTime forcedEndtime){
        Room room = entityManager.persist(new Room("Test", "Test", 20));
        Visitor expiredVisitor = entityManager.persist(new Visitor("expired"));

        RoomVisit roomVisit = new RoomVisit(
                expiredVisitor,
                room,
                TimeUtil.convertToDate(now.withHour(12).minusDays(1))
        );

        roomVisitRepository.save(roomVisit);

        // forcing check out at given time
        scheduledMaintenanceTasks.signOutAllVisitors(forcedEndtime);

        assertThat(roomVisitRepository.getRoomVisitorCount(room), is(0));
        assertThat(roomVisit.getCheckOutSource(), is(CheckOutSource.AutomaticCheckout));
        assertThat(roomVisit.getEndDate(), not(nullValue()));
    }
}
