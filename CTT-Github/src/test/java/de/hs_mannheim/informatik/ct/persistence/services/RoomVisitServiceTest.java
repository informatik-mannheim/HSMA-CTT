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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static de.hs_mannheim.informatik.ct.util.TimeUtil.convertToLocalDateTime;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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

    @Captor
    private ArgumentCaptor<List<RoomVisit>> roomVisitCaptor;

    private AutoCloseable mocks;

    @BeforeEach
    public void openMocks() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mocks.close();
    }

    @Test
    void autoCheckAutoVisitor_HadCheckedOutProperly() {
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
    void autoCheckAutoVisitor_NoCheckoutYesterday() {
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
    void autoCheckAutoVisitor_NoCheckoutToday() {
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
    void autoCheckAutoVisitor_NoCheckoutYesterdayAfterAutoCheckout() {
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
    void autoCheckAutoVisitor_NoCheckoutTodayAfterAutoCheckout() {
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