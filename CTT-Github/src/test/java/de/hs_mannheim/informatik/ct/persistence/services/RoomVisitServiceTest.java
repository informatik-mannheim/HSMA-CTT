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

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static de.hs_mannheim.informatik.ct.persistence.RoomVisitHelper.generateVisit;
import static de.hs_mannheim.informatik.ct.util.TimeUtil.convertToLocalDateTime;
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
    void checkOutAllVisitors() {
        val expected = new ArrayList<RoomVisit>();

        val yesterday = LocalDate.now().minusDays(1);
        val forcedEndTime = LocalTime.parse("18:00:00");
        val visitor = new Visitor("1");
        // No sign out yesterday
        expected.add(generateVisit(
                visitor,
                yesterday.atTime(LocalTime.parse("14:00:00")),
                yesterday.atTime(forcedEndTime))); // Expected Value, removed in mock call
        // No sign out today
        expected.add(generateVisit(
                visitor,
                LocalDate.now().atTime(LocalTime.parse("14:00:00")),
                LocalDate.now().atTime(forcedEndTime)));
        // Sign in yesterday after auto sign-in time
        expected.add(generateVisit(
                visitor,
                yesterday.atTime(forcedEndTime.plusHours(2)),
                yesterday.atTime(LocalTime.parse("23:59:59"))));

        val autoSignOutCandidates =
                expected.stream()
                        .peek((roomVisit -> roomVisit.setEndDate(null)))
                        .collect(Collectors.toList());

        // Sign in today after auto sign-in time
        autoSignOutCandidates.add(generateVisit(
                visitor,
                LocalDate.now().atTime(forcedEndTime.plusHours(2)),
                null));

        Mockito.when(roomVisitRepository.findNotCheckedOutVisits())
                .thenReturn(autoSignOutCandidates);

        roomVisitService.checkOutAllVisitors(forcedEndTime);

        Mockito
                .verify(
                        roomVisitRepository,
                        Mockito.times(1))
                .saveAll(roomVisitCaptor.capture());

        val capturedArgument = roomVisitCaptor.getValue();

        assertThat(capturedArgument, is(expected));
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
}