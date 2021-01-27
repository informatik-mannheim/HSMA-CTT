package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.model.Besucher;
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

        val roomVisits = new ArrayList<RoomVisit>();
        val expiredVisitor = new Besucher("expired");
        val notExpiredVisitor = new Besucher("not-expired");

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

        // Setup database
        entityManager.persist(notExpiredVisitor);
        entityManager.persist(expiredVisitor);
        entityManager.persist(roomVisits.get(0).getRoom());
        roomVisitRepository.saveAll(roomVisits);
        entityManager.flush();

        roomVisitRepository.deleteByEndBefore(TimeUtil.convertToDate(LocalDateTime.now().minusWeeks(4)));

        assertThat(roomVisitRepository.findAll(),
                everyItem(hasProperty("visitor", equalTo(notExpiredVisitor))));
    }
}
