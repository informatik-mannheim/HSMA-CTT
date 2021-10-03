/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.end_to_end;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties="allow_full_room_checkIn=true")      // this can only be set on class level, hence an extra test class is necessary
@TestPropertySource(properties="warning_for_full_room=false")
public class RoomControllerOverrideTest {
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

    private final String TEST_ROOM_NAME = "123";
    private String TEST_ROOM_PIN;
    private final String TEST_USER_EMAIL = "1233920@stud.hs-mannheim.de";

    @BeforeEach
    public void setUp() {
        Room room = new Room(TEST_ROOM_NAME, "A", 10);
        TEST_ROOM_PIN = room.getRoomPin();
        roomService.saveRoom(room);
    }
    
    @Test
    public void checkInFullRoomWithOverride() throws Exception {
        // find and fill testroom
        Room testRoom = roomService.findByName(TEST_ROOM_NAME).get();
        fillRoom(testRoom, 10);

        this.mockMvc.perform(
                get("/r/" + TEST_ROOM_NAME + "?override=true").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(null));

        this.mockMvc.perform(
                post("/r/checkInOverride")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("visitorEmail", TEST_USER_EMAIL)
                        .param("roomId", TEST_ROOM_NAME)
                        .param("roomPin", TEST_ROOM_PIN)
                        .with(csrf()))
                .andExpect(forwardedUrl(null))
                .andExpect(status().isOk());
    }

    /**
     * Helper method that creates users to fill room.
     * An address is created by combining iterator value with '@stud.hs-mannheim.de'.
     * To prevent the Test-User getting checked in, 0@stud.hs-mannheim.de is prevented as a fallback Address.
     *
     * @param room   the room that should get filled.
     * @param amount the amount the room will be filled.
     */
    public void fillRoom(Room room, int amount) throws InvalidEmailException, InvalidExternalUserdataException {

        for (int i = 0; i < amount; i++) {
            String randomUserEmail = String.format("%d@stud.hs-mannheim.de", i);

            if (randomUserEmail != TEST_USER_EMAIL) {
                roomVisitService.visitRoom(visitorService.findOrCreateVisitor("" + i + "@stud.hs-mannheim.de", null, null, null), room);
            } else {
                roomVisitService.visitRoom(visitorService.findOrCreateVisitor("0@stud.hs-mannheim.de", null, null, null), room);
            }
        }
    }
}
