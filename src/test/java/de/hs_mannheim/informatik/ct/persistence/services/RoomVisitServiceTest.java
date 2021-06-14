package de.hs_mannheim.informatik.ct.persistence.services;

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


import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.RoomVisitHelper;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.*;

import static de.hs_mannheim.informatik.ct.util.TimeUtil.convertToLocalDateTime;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
class RoomVisitServiceTest {
    @TestConfiguration
    static class RoomVisitServiceTestContextConfig {
        @Bean
        public RoomVisitService service() {
            return new RoomVisitService();
        }
    }

    @Autowired
    private RoomVisitService roomVisitService;

    @MockBean
    private RoomVisitRepository roomVisitRepository;

    @MockBean
    private VisitorRepository visitorRepository;

    @MockBean
    private DateTimeService dateTimeService;

    @MockBean
    private RoomVisit roomVisit;

    @Captor
    private ArgumentCaptor<List<RoomVisit>> roomVisitCaptor;

    private AutoCloseable mocks;

    private LocalDate today = LocalDate.of(2021, Month.APRIL, 2);
    private LocalDateTime now = LocalDateTime.of(today, LocalTime.of(18, 1));

    private LocalDate today = LocalDate.of(2021, Month.APRIL, 2);
    private LocalDateTime now = LocalDateTime.of(today, LocalTime.of(18, 1));

    @BeforeEach
    public void openMocks() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mocks.close();
    }

    @Test
    void autoCheckOutVisitor_HadCheckedOutProperly() {
        val forcedEndTime = LocalTime.of(18, 0);

        Mockito.when(roomVisitRepository.findNotCheckedOutVisits())
                .thenReturn(Collections.emptyList());

        roomVisitService.checkOutAllVisitors(forcedEndTime);

        Mockito
                .verify(
                        roomVisitRepository,
                        Mockito.times(1))
                .saveAll(roomVisitCaptor.capture());

        val capturedArgument = roomVisitCaptor.getValue();
        assertThat(capturedArgument.size(), equalTo(0));
    }

    @Test
    void autoCheckOutVisitor_NoCheckoutYesterday() {
        val forcedEndTime = LocalTime.of(18, 0);
        val today = LocalDate.of(2021, Month.APRIL, 2);
        val yesterday = today.minusDays(1);

        val checkInTime = LocalDateTime.of(yesterday, LocalTime.of(14, 0));
        val now = LocalDateTime.of(today, forcedEndTime.plusHours(1));

        val expectedCheckout = LocalDateTime.of(yesterday, forcedEndTime);

        Mockito.when(dateTimeService.getNow())
                .thenReturn(now);

        testAutoCheckout(checkInTime, forcedEndTime, expectedCheckout);
    }

    @Test
    void autoCheckOutVisitor_NoCheckoutToday() {
        val forcedEndTime = LocalTime.of(18, 0);
        val today = LocalDate.of(2021, Month.APRIL, 2);

        val checkInTime = LocalDateTime.of(today, LocalTime.of(14, 0));
        val now = LocalDateTime.of(today, forcedEndTime.plusHours(1));

        val expectedCheckout = LocalDateTime.of(today, forcedEndTime);

        Mockito.when(dateTimeService.getNow())
                .thenReturn(now);

        testAutoCheckout(checkInTime, forcedEndTime, expectedCheckout);
    }

    /**
     * Someone checked in yesterday AFTER the auto-checkout. They should be checked out at midnight.
     */
    @Test
    void autoCheckOutVisitor_NoCheckoutYesterdayAfterAutoCheckout() {
        val forcedEndTime = LocalTime.of(18, 0);
        val today = LocalDate.of(2021, Month.APRIL, 2);
        val yesterday = today.minusDays(1);

        val checkInTime = LocalDateTime.of(yesterday, LocalTime.of(18, 1));
        val now = LocalDateTime.of(today, forcedEndTime.plusHours(1));

        val expectedCheckout = LocalDateTime.of(yesterday, LocalTime.of(23, 59, 59));

        Mockito.when(dateTimeService.getNow())
                .thenReturn(now);

        testAutoCheckout(checkInTime, forcedEndTime, expectedCheckout);
    }

    /**
     * Someone checked in today AFTER the auto-checkout. They should not be checked out today.
     */
    @Test
    void autoCheckOutVisitor_NoCheckoutTodayAfterAutoCheckout() {
        val forcedEndTime = LocalTime.of(18, 0);
        val today = LocalDate.of(2021, Month.APRIL, 2);

        val checkInTime = LocalDateTime.of(today, LocalTime.of(18, 1));
        val now = LocalDateTime.of(today, forcedEndTime.plusHours(1));

        Mockito.when(dateTimeService.getNow())
                .thenReturn(now);

        testAutoCheckout(checkInTime, forcedEndTime, null);
    }

    @Test
    void deleteExpiredRecords() {
        roomVisitService.deleteExpiredRecords(Period.ofWeeks(4));

        val dateCaptor = ArgumentCaptor.forClass(Date.class);
        Mockito.verify(
                visitorRepository,
                Mockito.times(1)
        ).removeVisitorsWithNoVisits();
        Mockito.verify(
                roomVisitRepository,
                Mockito.times(1)
        ).deleteByEndDateBefore(dateCaptor.capture());

        val actualDate = convertToLocalDateTime(dateCaptor.getValue());
        val expectedDate = LocalDateTime.now().minusWeeks(4);

        assertThat(Duration.between(actualDate, expectedDate).toMinutes(), is(lessThan(1L)));
    }

    @Test
    void resetEmptyRoom(){
        Room emptyRoom = new Room("A", "B", 2);

        Mockito.when(roomVisitRepository.findNotCheckedOutVisits(emptyRoom))
                .thenReturn(Collections.EMPTY_LIST);

        roomVisitService.resetRoom(emptyRoom);

        Mockito.verify(roomVisitRepository).findNotCheckedOutVisits(emptyRoom);

        Mockito.verify(roomVisitRepository, Mockito.times(1))
                .saveAll(roomVisitCaptor.capture());
    }

    @Test
    void resetFilledRoom(){
        Visitor visitor = new Visitor("visitor");
        RoomVisit visit = new RoomVisitHelper(new Room("A", "B", 2)).generateVisit(
                visitor,
                this.now,
                null
        );

        // setup
        Room filledRoom = visit.getRoom();

        Mockito.when(roomVisitRepository.findNotCheckedOutVisits(filledRoom))
                .thenReturn(Collections.singletonList(visit));

        Mockito.when(dateTimeService.getDateNow())
                .thenReturn(java.util.Date.from(this.today.atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()));

        // method call
        roomVisitService.resetRoom(filledRoom);

        // behavior validation
        Mockito.verify(roomVisitRepository).findNotCheckedOutVisits(filledRoom);

        Mockito.verify(roomVisitRepository, Mockito.times(1))
                .saveAll(roomVisitCaptor.capture());

        assertThat(roomVisitService.getVisitorCount(filledRoom), equalTo(0));
    }

    @Test
    void resetFullRoom(){
        Room fullRoom = new Room("A", "B", 10);
        List<RoomVisit> roomVisits = new ArrayList<RoomVisit>();

        // fill roomVisits with Visitors who visit the room
        for(int i = 0; i < 10; i++){
            Visitor visitor = new Visitor("visitor" + i);
            RoomVisit visit = new RoomVisitHelper(fullRoom).generateVisit(
                    visitor,
                    this.now,
                    null
            );
            roomVisits.add(visit);
        }

        // setup
        Mockito.when(roomVisitRepository.findNotCheckedOutVisits(fullRoom))
                .thenReturn(roomVisits);

        Mockito.when(dateTimeService.getDateNow())
                .thenReturn(java.util.Date.from(this.today.atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()));

        // method call
        roomVisitService.resetRoom(fullRoom);

        // behavior validation
        Mockito.verify(roomVisitRepository).findNotCheckedOutVisits(fullRoom);

        Mockito.verify(roomVisitRepository, Mockito.times(1))
                .saveAll(roomVisitCaptor.capture());

        assertThat(roomVisitService.getVisitorCount(fullRoom), equalTo(0));
    }

    /**
     * resets a Room with visitors who already checked out and others who did not
     */
    @Test
    void resetRoomExpiredRecords()  {
        Visitor expiredVisitor = new Visitor("exp");
        Visitor notExpiredVisitor = new Visitor("nexp");
        Room testRoom = new Room("A", "B", 4);
        // adds expired and not expired visitors
        List<RoomVisit> visits = new RoomVisitHelper(testRoom).generateExpirationTestData(expiredVisitor, notExpiredVisitor);

        // setup
        Mockito.when(roomVisitRepository.findNotCheckedOutVisits(testRoom))
                .thenReturn(visits);

        Mockito.when(dateTimeService.getDateNow())
                .thenReturn(java.util.Date.from(this.today.atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()));

        // method call
        Assertions.assertThrows(AssertionError.class, () -> roomVisitService.resetRoom(testRoom));

        // behavior validation
        Mockito.verify(roomVisitRepository).findNotCheckedOutVisits(testRoom);

        assertThat(roomVisitService.getVisitorCount(testRoom), equalTo(0));
        for(RoomVisit visit : visits){
            assertThat(visit.getCheckOutSource(), not(RoomVisit.CheckOutSource.NotCheckedOut));
        }
    }

    @Test
    void resetRoom_chekOutSource(){
        Room testRoom = new Room("A", "B", 1);
        Visitor visitor = new Visitor("visitor");
        RoomVisit visit = new RoomVisitHelper(new Room("A", "B", 1)).generateVisit(
                visitor,
                this.now,
                null
        );

        // setup
        Mockito.when(roomVisitRepository.findNotCheckedOutVisits(testRoom))
                .thenReturn(Collections.singletonList(visit));
        Mockito.when(dateTimeService.getDateNow())
                .thenReturn(java.util.Date.from(this.today.atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()));

        roomVisitService.resetRoom(testRoom);

        assertThat(visit.getCheckOutSource(), equalTo(RoomVisit.CheckOutSource.RoomReset));
    }

    private void testAutoCheckout(@NonNull LocalDateTime checkInTime, @NonNull LocalTime forcedEndTime, LocalDateTime expectedCheckoutTime) {
        val visitor = new Visitor("1");
        val visit = new RoomVisitHelper(new Room("A", "A007a", 2)).generateVisit(
                visitor,
                checkInTime,
                null
        );

        Mockito.when(roomVisitRepository.findNotCheckedOutVisits())
                .thenReturn(Collections.singletonList(visit));

        roomVisitService.checkOutAllVisitors(forcedEndTime);

        Mockito
                .verify(
                        roomVisitRepository,
                        Mockito.times(1))
                .saveAll(roomVisitCaptor.capture());

        val capturedArgument = roomVisitCaptor.getValue();
        if (expectedCheckoutTime == null) {
            // No one to be checked out
            assertThat(capturedArgument.size(), equalTo(0));
        } else {
            assertThat(capturedArgument.size(), equalTo(1));
            val checkedOutVisit = capturedArgument.get(0);
            assertThat(convertToLocalDateTime(checkedOutVisit.getEndDate()), equalTo(expectedCheckoutTime));
            assertThat(checkedOutVisit.getCheckOutSource(), equalTo(RoomVisit.CheckOutSource.AutomaticCheckout));
        }
    }
}
