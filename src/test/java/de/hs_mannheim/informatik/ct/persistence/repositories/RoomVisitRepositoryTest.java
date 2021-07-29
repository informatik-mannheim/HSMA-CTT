package de.hs_mannheim.informatik.ct.persistence.repositories;

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
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.RoomVisitHelper;
import de.hs_mannheim.informatik.ct.util.TimeUtil;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RoomVisitRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomVisitRepository roomVisitRepository;

    private List<RoomVisit> visits;

    @Test
    public void deleteExpiredVisits() {
        val roomVisitHelper = new RoomVisitHelper(entityManager.persist(
                new Room("Test", "Test", 20)));
        val expiredVisitor = entityManager.persist(new Visitor("expired"));
        val notExpiredVisitor = entityManager.persist(new Visitor("not-expired"));
        val roomVisits = roomVisitHelper.generateExpirationTestData(
                expiredVisitor,
                notExpiredVisitor);

        roomVisitRepository.saveAll(roomVisits);
        entityManager.flush();

        roomVisitRepository.deleteByEndDateBefore(TimeUtil.convertToDate(LocalDateTime.now().minusWeeks(4)));

        assertThat(roomVisitRepository.findAll(),
                everyItem(hasProperty("visitor", equalTo(notExpiredVisitor))));
    }

    @Test
    public void findVisitsWithContact_MultipleContacts() {
        val roomVisitHelper = new RoomVisitHelper(entityManager.persist(new Room("Test", "Test", 20)));
        val target = entityManager.persist(new Visitor("target"));
        val contact1 = entityManager.persist(new Visitor("contact1"));
        val contact2 = entityManager.persist(new Visitor("contact2"));
        val targetVisit = entityManager.persist(
                roomVisitHelper.generateVisit(target,
                        LocalDateTime.parse("2021-03-20T10:00:00"), LocalDateTime.parse("2021-03-20T11:00:00")));
        entityManager.persist(
                roomVisitHelper.generateVisit(contact1,
                        LocalDateTime.parse("2021-03-20T10:15:00"), LocalDateTime.parse("2021-03-20T11:00:00")));
        entityManager.persist(
                roomVisitHelper.generateVisit(contact2,
                        LocalDateTime.parse("2021-03-20T10:00:00"), LocalDateTime.parse("2021-03-20T10:15:00")));
        entityManager.flush();

        val contacts = roomVisitRepository.findVisitsWithContact(target);

        assertThat(contacts.size(), equalTo(2));
        assertThat(contacts, everyItem(hasProperty("targetVisit", equalTo(targetVisit))));
    }

    @Test
    public void findNotCheckedOutVisitsTest() {
        altSetUp();
        roomVisitRepository.saveAll(this.visits);
        entityManager.flush();

        List<RoomVisit> notCheckedOutVisits = roomVisitRepository.findNotCheckedOutVisits();

        assertThat(notCheckedOutVisits, equalTo(this.visits));
    }

    @Test
    public void findNotCheckedOutVisits_RoomParam() {
        altSetUp();
        Room room = this.visits.get(0).getRoom();

        roomVisitRepository.saveAll(this.visits);
        entityManager.flush();

        List<RoomVisit> notCheckedOutVisits = roomVisitRepository.findNotCheckedOutVisits(room);

        assertThat(notCheckedOutVisits, equalTo(this.visits));
    }

    @Test
    public void findNotCheckedOutVisits_VisitorParam() {
        altSetUp();
        Visitor visitor = this.visits.get(0).getVisitor();

        roomVisitRepository.saveAll(this.visits);
        entityManager.flush();

        List<RoomVisit> notCheckedOutVisits = roomVisitRepository.findNotCheckedOutVisits(visitor);

        assertThat(notCheckedOutVisits, equalTo(this.visits));
    }

    @Test
    public void getRoomVisitorCount_smallRoom() {
        altSetUp();
        Room smallRoom = new Room("room", "a", 5);

        List<RoomVisit> fewVisits = generateVisitsForRoom(smallRoom, 2);
        roomVisitRepository.saveAll(fewVisits);
        entityManager.flush();

        assertThat(roomVisitRepository.getRoomVisitorCount(smallRoom), equalTo(2));
    }

    @Test
    public void getRoomVisitorCount_mediumRoom() {
        altSetUp();
        Room mediumRoom = new Room("medium", "a", 10);

        List<RoomVisit> someVisits = generateVisitsForRoom(mediumRoom, 5);
        roomVisitRepository.saveAll(someVisits);
        entityManager.flush();

        assertThat(roomVisitRepository.getRoomVisitorCount(mediumRoom), equalTo(5));
    }

    @Test
    public void getRoomVisitorCount_fullRoom() {
        altSetUp();
        Room filledRoom = new Room("full", "a", 10);

        List<RoomVisit> manyVisits = generateVisitsForRoom(filledRoom, 10);
        roomVisitRepository.saveAll(manyVisits);
        entityManager.flush();

        assertThat(roomVisitRepository.getRoomVisitorCount(filledRoom), equalTo(10));
    }

    @Test
    public void getAllStudyRooms_oneEmptyRoom() {
        val room = entityManager.persist(new Room("test", "a", 5));
        entityManager.flush();

        val studyRooms = roomVisitRepository.getAllStudyRooms(new String[]{"test"});

        assertThat(studyRooms.size(), equalTo(1));
        val studyRoom = studyRooms.get(0);
        assertThat(studyRoom.getRoomName(), equalTo(room.getName()));
        assertThat(studyRoom.getBuildingName(), equalTo(room.getBuildingName()));

        assertThat(studyRoom.getVisitorCount(), equalTo(0L));
    }

    @Test
    public void getAllStudyRooms_oneVisitorInRoom() {
        val room = entityManager.persist(new Room("test", "a", 5));
        RoomVisitHelper roomVisitHelper = new RoomVisitHelper(room);
        val visit = roomVisitHelper.generateVisit(
                entityManager.persist(new Visitor("hi@aol.com")),
                LocalDateTime.parse("2021-03-20T10:00:00"),
                null);

        entityManager.persist(visit);
        entityManager.flush();

        val studyRooms = roomVisitRepository.getAllStudyRooms(new String[]{"test"});

        assertThat(studyRooms.size(), equalTo(1));
        val studyRoom = studyRooms.get(0);
        assertThat(studyRoom.getRoomName(), equalTo(room.getName()));
        assertThat(studyRoom.getBuildingName(), equalTo(room.getBuildingName()));

        assertThat(studyRoom.getVisitorCount(), equalTo(1L));
    }

    @Test
    public void getAllStudyRooms_onePastVisitorInRoom() {
        val room = entityManager.persist(new Room("test", "a", 5));
        RoomVisitHelper roomVisitHelper = new RoomVisitHelper(room);
        val visit = roomVisitHelper.generateVisit(
                entityManager.persist(new Visitor("hi@aol.com")),
                LocalDateTime.parse("2021-03-20T10:00:00"),
                LocalDateTime.parse("2021-03-20T10:30:00"));

        entityManager.persist(visit);
        entityManager.flush();

        val studyRooms = roomVisitRepository.getAllStudyRooms(new String[]{"test"});

        assertThat(studyRooms.size(), equalTo(1));
        val studyRoom = studyRooms.get(0);
        assertThat(studyRoom.getRoomName(), equalTo(room.getName()));
        assertThat(studyRoom.getBuildingName(), equalTo(room.getBuildingName()));

        assertThat(studyRoom.getVisitorCount(), equalTo(0L));
    }

    @Test
    public void getTotalStudyVisitorCount_noRooms() {
        roomVisitRepository.getTotalStudyRoomsVisitorCount(new String[]{"test"});
    }

    /**
     * alternativ setup method. If this is set as default @Before Method some methods wont run
     */
    private void altSetUp() {
        Room room = new Room("Test", "Test", 20);
        RoomVisitHelper roomVisitHelper = new RoomVisitHelper(entityManager.persist(room));
        Visitor visitor = entityManager.persist(new Visitor("email"));

        this.visits = Stream.of(
                roomVisitHelper.generateVisit(
                        visitor,
                        LocalDateTime.now(),
                        null
                )
        ).collect(Collectors.toList());
    }

    /**
     * Generates a List of RoomVisits for given Room. The Visitor names are created as visitor0 - visitorX where X is @param visitorAmount
     *
     * @param room          Room object the generated visitors will visit.
     * @param visitorAmount amount of visitors that should visit the room.
     * @return List of RoomVisits.
     */
    private List<RoomVisit> generateVisitsForRoom(Room room, int visitorAmount) {
        RoomVisitHelper roomVisitHelper = new RoomVisitHelper(entityManager.persist(room));
        RoomVisit[] visitsList = new RoomVisit[visitorAmount];

        for (int i = 0; i < visitorAmount; i++) {
            Visitor visitor = entityManager.persist(new Visitor("visitor" + i));
            visitsList[i] = roomVisitHelper.generateVisit(
                    visitor,
                    LocalDateTime.now(),
                    null
            );
        }

        return Arrays.asList(visitsList);
    }
}
