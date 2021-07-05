package de.hs_mannheim.informatik.ct.model;

import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.util.TimeUtil;
import org.aspectj.weaver.tools.cache.CacheKeyResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
public class RoomVisitTest {
    private AutoCloseable mocks;

    @BeforeEach
    public void openMocks() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mocks.close();
    }

    @MockBean
    private Room room;

    @MockBean
    private Visitor visitor;

    private LocalDateTime now = LocalDateTime.now();

    @Test
    void checkout(){
        RoomVisit checkOutSourceProvider = new RoomVisit(visitor, room, TimeUtil.convertToDate(now));

        RoomVisit roomVisit = new RoomVisit(
                room,
                null,
                TimeUtil.convertToDate(now.minusDays(1)),
                TimeUtil.convertToDate(now),
                visitor,
                checkOutSourceProvider.getCheckOutSource());

        roomVisit.checkOut(TimeUtil.convertToDate(now), CheckOutSource.RoomReset);

        assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
    }

    @Test
    void checkout_checkOutSources(){
        Date validDate = TimeUtil.convertToDate(now);

        // no matter which check out source is used. The user will get checked out
        for(CheckOutSource checkOutSource : CheckOutSource.values()){
            RoomVisit roomVisit = checkOutCall(validDate, checkOutSource);
            assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
        }
    }

    @Test
    void checkout_endDate(){
        Date tomorrow = TimeUtil.convertToDate(now.plus(Duration.ofDays(1)));
        Date yesterday = TimeUtil.convertToDate(now.minus(Duration.ofDays(1)));


    }

    private RoomVisit checkOutCall(Date checkOutDate, CheckOutSource checkOutSource){
        RoomVisit roomVisit = new RoomVisit(
                room,
                null,
                TimeUtil.convertToDate(now.minusDays(1)),
                checkOutDate,
                visitor,
                checkOutSource
        );

        roomVisit.checkOut(TimeUtil.convertToDate(now), CheckOutSource.RoomReset);
        return roomVisit;
    }
}
