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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RoomVisitRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomVisitRepository roomVisitRepository;

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
}
