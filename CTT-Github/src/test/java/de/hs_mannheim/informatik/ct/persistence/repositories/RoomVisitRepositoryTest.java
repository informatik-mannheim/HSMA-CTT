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

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
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
        assertThat(entityManager, notNullValue());

        val expiredVisitor = new Visitor("expired");
        val notExpiredVisitor = new Visitor("not-expired");
        val roomVisits = generateExpirationTestData(expiredVisitor, notExpiredVisitor);

        // Setup database
        entityManager.persist(notExpiredVisitor);
        entityManager.persist(expiredVisitor);
        entityManager.persist(roomVisits.get(0).getRoom());
        roomVisitRepository.saveAll(roomVisits);
        entityManager.flush();

        roomVisitRepository.deleteByEndDateBefore(TimeUtil.convertToDate(LocalDateTime.now().minusWeeks(4)));

        assertThat(roomVisitRepository.findAll(),
                everyItem(hasProperty("visitor", equalTo(notExpiredVisitor))));
    }

    /**
     * Generates a list of room visits, some of which should be delete because they are after the expiration date for personal data.
     * @param expiredVisitor The visitor used for visits that should be deleted
     * @param notExpiredVisitor The vistor used for visits that are still valid
     */
    public static List<RoomVisit> generateExpirationTestData(Visitor expiredVisitor, Visitor notExpiredVisitor) {
        val roomVisits = new ArrayList<RoomVisit>();

        // Absolutely not expired
        roomVisits.add(RoomVisitHelper.generateVisit(
                notExpiredVisitor,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()));

        // Older but also not expired
        roomVisits.add(RoomVisitHelper.generateVisit(
                notExpiredVisitor,
                LocalDateTime.now().minusHours(1).minusDays(25),
                LocalDateTime.now().minusDays(25)));

        // Definitely expired
        roomVisits.add(RoomVisitHelper.generateVisit(
                expiredVisitor,
                LocalDateTime.now().minusHours(1).minusMonths(2),
                LocalDateTime.now().minusMonths(2)));

        // Just expired
        roomVisits.add(RoomVisitHelper.generateVisit(
                expiredVisitor,
                LocalDateTime.now().minusHours(1).minusWeeks(4).minusDays(1),
                LocalDateTime.now().minusWeeks(4).minusDays(1)));

        return roomVisits;
    }
}
