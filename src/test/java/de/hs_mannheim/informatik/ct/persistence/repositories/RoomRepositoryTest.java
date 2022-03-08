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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.persistence.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.hs_mannheim.informatik.ct.model.Room;
import lombok.val;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RoomRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    public void findByNameIgnoreCase() {
        val unique = entityManager.persist(new Room("UNIQUE", "A", 20));
        val ambiguous1 = entityManager.persist(new Room("ambiguous", "A", 20));
        entityManager.persist(new Room("aMbIgUoUs", "A", 20));

        Assertions.assertEquals(roomRepository.findByNameIgnoreCase(unique.getName()).get(), unique);
        Assertions.assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> roomRepository.findByNameIgnoreCase(ambiguous1.getName()));
    }

    @Test
    public void verifyRoomPin() {
        entityManager.persist(new Room("pintest", "A", 20, "0007"));
        Assertions.assertEquals("0007", roomRepository.findById("pintest").get().getRoomPin());

    }

    @Test
    public void getStudyRoomTotalCapacity_noRooms() {
        roomRepository.getTotalStudyRoomsCapacity(new String[]{"test"});
    }

    @Test
    public void getStudyRoomTotalCapacity_oneMatchingRoom() {
        entityManager.persist(new Room("test", "t", 30));
        entityManager.persist(new Room("test_noStudy", "t", 20));
        val capacity = roomRepository.getTotalStudyRoomsCapacity(new String[]{"test"});
        assertThat(capacity, equalTo(30));
    }
}
