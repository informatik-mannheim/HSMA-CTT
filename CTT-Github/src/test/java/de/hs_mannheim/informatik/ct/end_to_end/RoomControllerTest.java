package de.hs_mannheim.informatik.ct.end_to_end;

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
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class RoomControllerTest {
    @TestConfiguration
    static class RoomControllerTestConfig {
        @Bean
        public RoomService service() {
            return new RoomService();
        }

    }

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private MockMvc mockMvc;

    private final String TEST_ROOM_NAME = "asdf";
    private final String USER_EMAIL = "123@stud.hs-mannheim.de";

    @Test
    public void isTestRoomActive() throws Exception {
        roomService.saveRoom(new Room(TEST_ROOM_NAME, "A", 10));

        this.mockMvc.perform(
                get("/r/" + this.TEST_ROOM_NAME).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("HSMA CTT - Check-in Raum " + this.TEST_ROOM_NAME)));
    }

    @Test
    public void checkInEmptyRoom() throws Exception {
        roomService.saveRoom(new Room(TEST_ROOM_NAME, "A", 10));

        this.mockMvc.perform(
                post("/r/checkIn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("visitorEmail", USER_EMAIL)
                        .param("roomId", TEST_ROOM_NAME)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void checkInFilledRoom() throws Exception {
        try {
            // fill room with people
            fillRoom(
                    // save initiated Room in DB
                    roomService.saveRoom(new Room(TEST_ROOM_NAME, "A", 10))
                    , 5);
        } catch(InvalidEmailException mailError) {

        }

        this.mockMvc.perform(
                post("/r/checkIn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("visitorEmail", USER_EMAIL)
                        .param("roomId", TEST_ROOM_NAME)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void checkInFullRoom() throws Exception {
        try {
            // fill room with people
            fillRoom(
                    // save initiated Room in DB
                    roomService.saveRoom(new Room(TEST_ROOM_NAME, "A", 10))
                    , 10);
        } catch(InvalidEmailException mailError) {

        }

        // request form to check into full room should redirect to roomFull/{roomId}
        this.mockMvc.perform(
                get("/r/" + TEST_ROOM_NAME).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("roomFull/" + TEST_ROOM_NAME));

    }

    @Test
    public void checkOut() throws Exception {
        roomService.saveRoom(new Room(TEST_ROOM_NAME, "A", 10));

        // post request on /r/checkout should redirect to /r/checkedOut
        this.mockMvc.perform(
                // check in
                post("/r/checkIn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("visitorEmail", USER_EMAIL)
                        .param("roomId", TEST_ROOM_NAME)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        // check out
                        result -> mockMvc.perform(
                                post("/r/checkOut")
                                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                        .param("visitorEmail", USER_EMAIL)
                                        .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isFound())
                                .andExpect(redirectedUrl("/r/checkedOut"))
                );
    }

    // TODO post on {roomId}/checkout
    //  check for and implement not tested method functionalities
    //  move room filling functionality into helperclass

    public void checkOutOfSpecifiedRoom(){

    }

    public void fillRoom(Room room, int amount) throws InvalidEmailException {
        for (int i = 0; i < amount; i++) {
            roomVisitService.visitRoom(visitorService.findOrCreateVisitor("" + i + "@stud.hs-mannheim.de"), room);
        }
    }
}
