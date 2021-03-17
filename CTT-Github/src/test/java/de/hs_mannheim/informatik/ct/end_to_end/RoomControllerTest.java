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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// TODO
//  test static sites: r/fullRoom with check on 'Raum voll' in content
//  create helper class to fill room
//  find process to checkout all test users from all test rooms in @AfterEach

@SpringBootTest
@AutoConfigureMockMvc
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

    private final String TEST_ROOM = "asdf";
    private final String USER_EMAIL = "123@stud.hs-mannheim.de";

    @BeforeEach
    public void setUp() {
        Room testRoom = roomService.saveRoom(new Room(TEST_ROOM, "A", 10));
    }

    @AfterEach
    public void cleanUp() {
        try {
            roomVisitService.checkOutVisitor(visitorService.findOrCreateVisitor(USER_EMAIL));
        } catch (InvalidEmailException e){
        }
    }

    @Test
    public void isTestRoomActive() throws Exception {
        this.mockMvc.perform(
                get("/r/" + this.TEST_ROOM))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("HSMA CTT - Check-in Raum " + this.TEST_ROOM)));
    }

    @Test
    public void checkIn() throws Exception {
        // empty Room
        this.mockMvc.perform(
                post("/r/checkIn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("visitorEmail", USER_EMAIL)
                        .param("roomId", TEST_ROOM)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // filled Room
        String filledRoomName = "A021";
        Room filledRoom = roomService.saveRoom(new Room(filledRoomName, "A", 10));
        fillRoom(filledRoom, 5);

        this.mockMvc.perform(
                post("/r/checkIn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("visitorEmail", USER_EMAIL)
                        .param("roomId", filledRoomName)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // full Room
        String fullRoomName = "A022";
        Room fullRoom = roomService.saveRoom(new Room(fullRoomName, "A", 10));
        fillRoom(fullRoom, 10);

        this.mockMvc.perform(
                get("/r/" + fullRoomName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("Raum voll")));
    }

    @Test
    public void checkOut() throws Exception {
        // post request on /r/checkout should redirect to /r/checkedOut
        this.mockMvc.perform(
                // check in
                post("/r/checkIn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("visitorEmail", USER_EMAIL)
                        .param("roomId", TEST_ROOM)
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

    public void fillRoom(Room room, int amount) throws Exception {
        for (int i = 1; i < amount; i++) {
            roomVisitService.visitRoom(visitorService.findOrCreateVisitor("" + i + "@stud.hs-mannheim.de"), room);
        }
    }
}
