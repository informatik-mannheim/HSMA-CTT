package de.hs_mannheim.informatik.ct.persistence.integration;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.RoomVisitHelper;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepositoryTest;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.Period;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;


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
        val expiredVisitor = new Visitor("expired");
        val notExpiredVisitor = new Visitor("not-expired");
        val partiallyExpiredVisitor = new Visitor("some-expired");

        entityManager.persist(notExpiredVisitor);
        entityManager.persist(expiredVisitor);
        entityManager.persist(partiallyExpiredVisitor);

        val roomVisits = RoomVisitRepositoryTest.generateExpirationTestData(expiredVisitor, notExpiredVisitor);

        // Add visits for a visitor that has an expired visit, but also a visit that is kept on record
        // A visit that just ended
        roomVisits.add(RoomVisitHelper.generateVisit(
                partiallyExpiredVisitor,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()));
        // A visit that ended two months ago, to be removed
        roomVisits.add(RoomVisitHelper.generateVisit(
                partiallyExpiredVisitor,
                LocalDateTime.now().minusHours(1).minusMonths(2),
                LocalDateTime.now().minusMonths(2)));

        // Store all records in the test db
        entityManager.persist(roomVisits.get(0).getRoom());
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
