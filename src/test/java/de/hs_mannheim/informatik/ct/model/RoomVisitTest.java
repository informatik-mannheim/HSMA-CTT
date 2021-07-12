package de.hs_mannheim.informatik.ct.model;

import de.hs_mannheim.informatik.ct.util.ScheduledMaintenanceTasks;
import de.hs_mannheim.informatik.ct.util.TimeUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

        assertThat(roomVisit.getEndDate(), notNullValue());
        assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
    }

    @Test
    void checkout_checkOutSources(){
        Date validDate = TimeUtil.convertToDate(now);

        // no matter which check out source is used. The user will get checked out
        for(CheckOutSource checkOutSource : CheckOutSource.values()){
            RoomVisit roomVisit = checkOutCall(validDate, checkOutSource);
            assertThat(roomVisit.getEndDate(), notNullValue());
            assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
        }
    }

    @Test
    void automaticCheckOutFailureData_checkOut() {
        Date validDate = TimeUtil.convertToDate(now);

        Long[] idArray = new Long[]{188L, 192L, 194L, 133L, 123L};

        Visitor[] visitors = createVisitors_automaticCheckOutFailureData();
        Room[] rooms = createRooms_automaticCheckoutFailureData();

        for(int i = 0; i < visitors.length; i++){
            RoomVisit roomVisit = new RoomVisit(
                    rooms[i],
                    idArray[i],
                    TimeUtil.convertToDate(now.minusDays(1)),
                    validDate,
                    visitors[i],
                    CheckOutSource.AutomaticCheckout
            );

            roomVisit.checkOut(TimeUtil.convertToDate(now), CheckOutSource.RoomReset);
            assertThat(roomVisit.getEndDate(), notNullValue());
            assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
        }
    }

    @Test
    void automaticCheckOutFailureData_signOutAllVisitors() {
        Date validDate = TimeUtil.convertToDate(now.minusMinutes(1));

        Long[] idArray = new Long[]{188L, 192L, 194L, 133L, 123L};

        Room[] rooms = createRooms_automaticCheckoutFailureData();
        Visitor[] visitors = createVisitors_automaticCheckOutFailureData();

        ScheduledMaintenanceTasks scheduledMaintenanceTasks = new ScheduledMaintenanceTasks();

        for(int i = 0; i < visitors.length; i++){
            RoomVisit roomVisit = new RoomVisit(
                    rooms[i],
                    idArray[i],
                    TimeUtil.convertToDate(now.minusDays(1)),
                    validDate,
                    visitors[i],
                    CheckOutSource.AutomaticCheckout
            );

            scheduledMaintenanceTasks.signOutAllVisitors(LocalTime.now());
            assertThat(roomVisit.getEndDate(), notNullValue());
            assertThat(roomVisit.getCheckOutSource(), not(CheckOutSource.NotCheckedOut));
        }
    }


    /* Database entry that caused visitors to not get checked out automatically
     *         id  | end_date | start_date                 | room_name | visitor_id | check_out_source
     *         ---- - +-------------------------+-------------------------+-----------+------------+------------------
     *         188 |          |2021 - 07 - 01 12:29:54.322 | r1        | 187        | 0
     *         192 |          |2021 - 07 - 01 12:42:29.346 | r2        | 191        | 0
     *         194 |          |2021 - 07 - 01 12:43:08.854 | r3        | 193        | 0
     *         133 |          |2021 - 06 - 20 12:07:31.253 | r4        | 27         |
     *         123 |          |2021 - 06 - 17 15:05:49.142 | r5        | 122        |
     */

    /**
     * Creates objects after a db dump where users did not get checked automatically
     * @return visitors that did not get checked out
     */
    private Visitor[] createVisitors_automaticCheckOutFailureData(){
        Visitor visitor1 = new Visitor(187L, "v1");
        Visitor visitor2 = new Visitor(191L, "v2");
        Visitor visitor3 = new Visitor(193L, "v3");
        Visitor visitor4 = new Visitor(27L, "v4");
        Visitor visitor5 = new Visitor(122L, "v5");

        return new Visitor[]{visitor1, visitor2, visitor3, visitor4, visitor5};
    }

    /**
     * Creates objects after a db dump where users did not get checked automatically
     * @return censored rooms
     */
    private Room[] createRooms_automaticCheckoutFailureData(){
        Room room1 = new Room("r1", "L", 10);
        Room room2 = new Room("r2", "L", 10);
        Room room4 = new Room("r3", "K", 10);
        Room room5 = new Room("r4", "L", 10);

        return new Room[]{room1, room2, room2, room4, room5};
    }

    /**
     * Imitates check out for given checkOutSource by creating RoomVisit and checking out visitor.
     * @param checkOutDate Date Object specifying user check out
     * @param checkOutSource Entry from CheckOutSource specifying why user got checked out
     * @return room visit with user checked out
     */
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
