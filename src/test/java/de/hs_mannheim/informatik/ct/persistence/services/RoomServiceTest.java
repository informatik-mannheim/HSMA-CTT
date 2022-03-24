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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.persistence.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {
    @MockBean
    private RoomRepository roomRepositoryMock;
    @InjectMocks
    private RoomService roomService;

    @Test
    public void shouldThrowException_When_RaumPinIsNotANumberOrNull() {
        Room roomWithNullPin = new Room();
        roomWithNullPin.setRoomPin(null);
        Room notValidRoom = new Room();
        notValidRoom.setRoomPin("notValidPin");
        when(roomRepositoryMock.findAll()).thenReturn(Collections.emptyList());

        // Not necessary for the test for the test to pass, but when the test fails it
        // may mislead the real issue if not present.
        when(roomRepositoryMock.saveAll(Collections.singletonList(notValidRoom)))
                .thenReturn(Collections.singletonList(notValidRoom));
       assertAll(
               () -> assertThrows(IllegalArgumentException.class, () -> roomService.saveRoom(notValidRoom)),
               () -> assertThrows(IllegalArgumentException.class, () -> roomService.saveRoom(roomWithNullPin))
               );
    }
}
