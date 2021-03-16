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

import de.hs_mannheim.informatik.ct.controller.RoomController;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Visitor;
import org.checkerframework.checker.units.qual.A;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;

import java.util.Date;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTest {
    @TestConfiguration
    static class RoomControllerTestConfig {
        @Bean
        public RoomService service(){ return new RoomService();}
    }

    @Autowired
    RoomService roomService;

    @Autowired
    MockMvc mockMvc;

    // predefined Testroom. Delete as soon as Mock or helper works
    private final String TEST_ROOM = "A007a";
    private final String USER_EMAIL = "123@stud.hs-mannheim.de";

    private RoomControllerTestHelper helper;

    @Test
    public void isTestRoomAccessableTest() throws Exception {
        helper.saveTestRoom();

        Optional<Room> testRoom = roomService.findByName(helper.getRoomName());

        assertThat(testRoom.isPresent(), notNullValue());

        assertThat(testRoom.get().getId(), equalTo(this.TEST_ROOM));

        this.mockMvc.perform(
                get("/r/" + this.TEST_ROOM)).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(content().string(
                        not(containsString("HSMA CTT - Check-in Raum" + this.TEST_ROOM)))
                );
    }

    @Test
    public void checkIn() throws Exception {
        Visitor user = new Visitor(USER_EMAIL);
        //Mockito.when(visitorService.findOrCreateVisitor(anyString())).thenReturn();

        this.mockMvc.perform(
                post("/r/checkIn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("_csrf", "0ea3f156-44ca-4d75-b61c-1a05af87d0df")
                        .param("visitorEmail", USER_EMAIL)
                        .param("roomId", TEST_ROOM))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
