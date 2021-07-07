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
        RoomVisit roomVisit = new RoomVisit(
                room,
                null,
                TimeUtil.convertToDate(now.minusDays(1)),
                TimeUtil.convertToDate(now),
                visitor,
                CheckOutSource.NotCheckedOut);

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
    void automaticCheckOutFailureData() {
        /*
        this Data caused users not getting checked for an unknown cause
        id  | end_date | start_date                 | room_name | visitor_id | check_out_source
        ---- - +-------------------------+-------------------------+-----------+------------+------------------
        188 |          |2021 - 07 - 01 12:29:54.322 | L114      | 187        | 0
        192 |          |2021 - 07 - 01 12:42:29.346 | L206      | 191        | 0
        194 |          |2021 - 07 - 01 12:43:08.854 | L206      | 193        | 0
        133 |          |2021 - 06 - 20 12:07:31.253 | K019a     | 27         |
        123 |          |2021 - 06 - 17 15:05:49.142 | L312      | 122        |
        */
        Date validDate = TimeUtil.convertToDate(now);

        Visitor visitor1 = new Visitor(187L, "v1");
        Visitor visitor2 = new Visitor(191L, "v2");
        Visitor visitor3 = new Visitor(193L, "v3");
        Visitor visitor4 = new Visitor(27L, "v4");
        Visitor visitor5 = new Visitor(122L, "v5");

        Room room1 = new Room("L114", "L", 10);
        Room room2 = new Room("L206", "L", 10);
        Room room4 = new Room("K019a", "K", 10);
        Room room5 = new Room("L312", "L", 10);

        Visitor[] visitors = new Visitor[]{visitor1, visitor2, visitor3, visitor4, visitor5};
        Room[] rooms = new Room[]{room1, room2, room2, room4, room5};

        for(int i = 0; i < visitors.length; i++){
            RoomVisit roomVisit = checkOutCall(validDate, CheckOutSource.AutomaticCheckout, rooms[i], visitors[i]);
            assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
        }
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

    private RoomVisit checkOutCall(Date checkOutDate, CheckOutSource checkOutSource, Room customRoom, Visitor customVisitor){
        RoomVisit roomVisit = new RoomVisit(
                customRoom,
                null,
                TimeUtil.convertToDate(now.minusDays(1)),
                checkOutDate,
                customVisitor,
                checkOutSource
        );

        roomVisit.checkOut(TimeUtil.convertToDate(now), CheckOutSource.RoomReset);
        return roomVisit;
    }
}
