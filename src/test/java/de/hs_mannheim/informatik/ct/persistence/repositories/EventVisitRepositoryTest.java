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

package de.hs_mannheim.informatik.ct.persistence.repositories;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.hs_mannheim.informatik.ct.model.Event;
import de.hs_mannheim.informatik.ct.model.EventVisit;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Visitor;
import lombok.val;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class EventVisitRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventVisitRepository eventVisitRepository;

    @Test
    public void findVisitsWithContact_MultipleContacts() {
        val event = entityManager.persist(
                new Event(
                        "TestEvent",
                        entityManager.persist(new Room("test", "test", 20)),
                        Date.from(Instant.parse("2021-03-20T10:00:00Z")),
                        "target"));
        val target = entityManager.persist(new Visitor("target"));
        val contact1 = entityManager.persist(new Visitor("contact1"));
        val contact2 = entityManager.persist(new Visitor("contact2"));

        val targetVisit = new EventVisit(event, target, Date.from(Instant.parse("2021-03-20T10:00:00Z")));
        targetVisit.setEndDate(Date.from(Instant.parse("2021-03-20T11:00:00Z")));
        val contactVisit1 = new EventVisit(event, contact1, Date.from(Instant.parse("2021-03-20T10:15:00Z")));
        contactVisit1.setEndDate(Date.from(Instant.parse("2021-03-20T11:00:00Z")));
        val contactVisit2 = new EventVisit(event, contact2, Date.from(Instant.parse("2021-03-20T10:00:00Z")));
        contactVisit2.setEndDate(Date.from(Instant.parse("2021-03-20T10:15:00Z")));
        entityManager.persist(targetVisit);
        entityManager.persist(contactVisit1);
        entityManager.persist(contactVisit2);
        entityManager.flush();

        val contacts = eventVisitRepository.findVisitsWithContact(target);

        assertThat(contacts.size(), equalTo(2));
        assertThat(contacts, everyItem(hasProperty("targetVisit", equalTo(targetVisit))));
    }
}
