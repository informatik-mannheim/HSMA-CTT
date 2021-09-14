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
import de.hs_mannheim.informatik.ct.persistence.RoomVisitHelper;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.EventVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.util.ScheduledMaintenanceTasks;
import de.hs_mannheim.informatik.ct.util.TimeUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;
@ExtendWith(SpringExtension.class)
@DataJpaTest
public class CheckOutTest {
    @TestConfiguration
    static class RoomVisitServiceTestContextConfig {
        @Bean
        public RoomVisitService service() {
            return new RoomVisitService();
        }

        @Bean
        public DateTimeService dateTimeService() {
            return new DateTimeService();
        }

        @Bean
        public EventVisitService eventVisitService() {
            return new EventVisitService();
        }

        @Bean
        public ScheduledMaintenanceTasks scheduledMaintenanceTasks() {
            return new ScheduledMaintenanceTasks();
        }
    }

    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomVisitRepository roomVisitRepository;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private ScheduledMaintenanceTasks scheduledMaintenanceTasks;

    private LocalDateTime now = LocalDateTime.now();

    @Test
    void scheduledMaintenanceTasksTest() {
        Room room = entityManager.persist(new Room("A001", "A", 10));
        List<RoomVisit> visitsToday = Arrays.asList(
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("1")),
                        room,
                        //TimeUtil.convertToDate(LocalDateTime.parse("03:54:59"))
                        TimeUtil.convertToDate(now.withHour(3).withMinute(54).withSecond(59))
                )),
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("2")),
                        room,
                        //TimeUtil.convertToDate(LocalDateTime.parse("00:00:00"))
                        TimeUtil.convertToDate(now.withHour(0).withMinute(0).withSecond(0))
                )),
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("3")),
                        room,
                        //TimeUtil.convertToDate(LocalDateTime.parse("12:00:00"))
                        TimeUtil.convertToDate(now.withHour(12).withMinute(0).withSecond(0))
                ))
        );
        List<RoomVisit> visitsYesterday = Arrays.asList(
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("4")),
                        room,
                        //TimeUtil.convertToDate(LocalDateTime.parse("23:59:59").minusDays(1))
                        TimeUtil.convertToDate(now.withHour(23).withMinute(59).withSecond(59).minusDays(1))
                )),
                entityManager.persist(new RoomVisit(
                        entityManager.persist(new Visitor("5")),
                        room,
                        //TimeUtil.convertToDate(LocalDateTime.parse("00:00:01").minusDays(1))
                        TimeUtil.convertToDate(now.withHour(0).withMinute(0).withSecond(1).minusDays(1))
                ))
        );

        roomVisitRepository.saveAll(visitsToday);
        roomVisitRepository.saveAll(visitsYesterday);

        // this ensures that RoomVisitService::checkOutAllVisitors works as if it was 3:55. This is the time the cron job should run
        //when(dateTimeService.getNow()).thenReturn(LocalDateTime.parse("03:55:00"));

        // run checkout routine
        scheduledMaintenanceTasks.doMaintenance();

        assertThat(roomVisitRepository.findNotCheckedOutVisits(room),
                everyItem(in(visitsToday)));
        assertThat(roomVisitRepository.findNotCheckedOutVisits(room),
                not(everyItem(in(visitsYesterday))));

        visitsToday.stream().allMatch(
                visit -> {
                    return
                            visit.getCheckOutSource().equals(CheckOutSource.NotCheckedOut) &&
                                    visit.getEndDate() == null;
                }
        );

        visitsYesterday.stream().allMatch(
                visit -> {
                    return
                            visit.getCheckOutSource().equals(CheckOutSource.AutomaticCheckout) &&
                                    visit.getEndDate() != null;
                }
        );

    }

    @Test
    void checkout() {
        RoomVisit roomVisit = new RoomVisit(
                new Room("room", "A", 1),
                null,
                TimeUtil.convertToDate(now.minusDays(1)),
                null,
                new Visitor("checkout"),
                CheckOutSource.NotCheckedOut
        );

        roomVisit.checkOut(TimeUtil.convertToDate(now), CheckOutSource.RoomReset);

        assertThat(roomVisit.getEndDate(), notNullValue());
        assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
    }

    @Test
    void recurringForceCheckOut_midnight() {
        Room room = new Room("Test", "Test", 20);
        RoomVisitHelper roomVisitHelper = new RoomVisitHelper(entityManager.persist(room));
        Visitor expiredVisitor = entityManager.persist(new Visitor("expired"));

        RoomVisit roomVisit = new RoomVisit(
                room,
                null,
                TimeUtil.convertToDate(now.minusDays(1).withHour(12)),
                null,
                expiredVisitor,
                CheckOutSource.NotCheckedOut
        );

        roomVisitRepository.save(roomVisit);
        // forcing check out at 00:00
        scheduledMaintenanceTasks.signOutAllVisitors(LocalTime.of(0, 0));

        assertThat(roomVisitRepository.getRoomVisitorCount(room), is(0));
        assertThat(roomVisit.getCheckOutSource(), is(CheckOutSource.AutomaticCheckout));
        assertThat(roomVisit.getEndDate(), not(nullValue()));
    }

    @Test
    void recurringForceCheckOut_morning() {
        Room room = new Room("Test", "Test", 20);
        RoomVisitHelper roomVisitHelper = new RoomVisitHelper(entityManager.persist(room));
        Visitor expiredVisitor = entityManager.persist(new Visitor("expired"));

        RoomVisit roomVisit = new RoomVisit(
                room,
                null,
                TimeUtil.convertToDate(now.withHour(1)),
                null,
                expiredVisitor,
                CheckOutSource.NotCheckedOut
        );

        roomVisitRepository.save(roomVisit);
        // forcing check out at 03:55
        scheduledMaintenanceTasks.signOutAllVisitors(LocalTime.of(3, 55));

        assertThat(roomVisitRepository.getRoomVisitorCount(room), is(0));
        assertThat(roomVisit.getCheckOutSource(), is(CheckOutSource.AutomaticCheckout));
        assertThat(roomVisit.getEndDate(), not(nullValue()));
    }
}
