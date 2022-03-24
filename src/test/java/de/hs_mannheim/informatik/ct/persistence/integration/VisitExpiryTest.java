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

package de.hs_mannheim.informatik.ct.persistence.integration;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.time.Period;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.RoomVisitHelper;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import lombok.val;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class VisitExpiryTest {
    @TestConfiguration
    static class RoomVisitServiceTestContextConfig {
        @Bean
        public RoomVisitService service() {
            return new RoomVisitService();
        }

        @Bean
        public DateTimeService dateTimeService() {
            return new DateTimeService();
        }
    }

    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomVisitRepository roomVisitRepository;

    @Autowired
    private VisitorRepository visitorRepository;

    /**
     * This test creates room visits, some of which are expired, and verifies that room visits older than the deletion
     * date are removed. If a user has no more room visits remaining on record, they have to be removed as well.
     */
    @Test
    public void expirePersonalData() {
        val roomVisitHelper = new RoomVisitHelper(entityManager.persist(
                new Room("Test", "Test", 20)));
        val expiredVisitor = entityManager.persist(new Visitor("expired"));
        val notExpiredVisitor = entityManager.persist(new Visitor("not-expired"));
        val partiallyExpiredVisitor = entityManager.persist(new Visitor("some-expired"));

        val roomVisits = roomVisitHelper.generateExpirationTestData(expiredVisitor, notExpiredVisitor);

        // Add visits for a visitor that has an expired visit, but also a visit that is kept on record
        // A visit that just ended
        roomVisits.add(roomVisitHelper.generateVisit(
                partiallyExpiredVisitor,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()));
        // A visit that ended two months ago, to be removed
        roomVisits.add(roomVisitHelper.generateVisit(
                partiallyExpiredVisitor,
                LocalDateTime.now().minusHours(1).minusMonths(2),
                LocalDateTime.now().minusMonths(2)));

        roomVisitRepository.saveAll(roomVisits);

        // Expire records older than 4 Weeks
        roomVisitService.deleteExpiredRecords(Period.ofWeeks(4));

        // Verify that visit are expired
        assertThat(roomVisitRepository.findAll(),
                everyItem(hasProperty("visitor",
                        anyOf(equalTo(notExpiredVisitor), equalTo(partiallyExpiredVisitor)))));

        // Verify that only the expired visitor was removed
        assertThat(visitorRepository.findAll().size(), equalTo(2));
        assertThat(visitorRepository.findAll(),
                everyItem(anyOf(equalTo(notExpiredVisitor), equalTo(partiallyExpiredVisitor))));
    }
}
