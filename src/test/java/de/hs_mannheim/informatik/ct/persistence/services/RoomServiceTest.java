package de.hs_mannheim.informatik.ct.persistence.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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
    private  RoomService roomService;

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
