package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.model.Room;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RoomRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void findByNameIgnoreCase() {
        val unique = entityManager.persist(new Room("UNIQUE", "A", 20));
        val ambiguous1 = entityManager.persist(new Room("ambiguous", "A", 20));
        val ambiguous2 = entityManager.persist(new Room("aMbIgUoUs", "A", 20));

        Assertions.assertEquals(roomRepository.findByNameIgnoreCase(unique.getName()).get(), unique);
        Assertions.assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> roomRepository.findByNameIgnoreCase(ambiguous1.getName()));
    }

    @Test
    public void verifyRoomPin() {
        Room room = new Room("pintest", "A", 20);
        String roomPinBeforePersistance = room.getRoomPin();
        val pinTestRoom = entityManager.persist(room);
        Assertions.assertEquals(roomPinBeforePersistance, roomRepository.findById("pintest").get().getRoomPin());
    }
}
