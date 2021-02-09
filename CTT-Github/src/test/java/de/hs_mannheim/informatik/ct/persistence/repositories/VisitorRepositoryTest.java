package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class VisitorRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisitorRepository visitorRepository;

    private final Visitor visitor1 = new Visitor("12345@stud.hs-mannheim.de");
    private final Visitor visitor2 = new Visitor("13337@stud.hs-mannheim.de");
    private final Visitor visitor3 = new Visitor("77777@stud.hs-mannheim.de");

    private void befuelleDatenbank() {
        Room room = new Room("A008", 2);
        Veranstaltung veranstaltung1 = new Veranstaltung("PR1", room, new Date(), "Herr Müller");
        Veranstaltung veranstaltung2 = new Veranstaltung("PR2", room, new Date(), "Frau Meier");

        entityManager.persist(veranstaltung1);
        entityManager.persist(veranstaltung2);

        VeranstaltungsBesuch besuch1 = new VeranstaltungsBesuch(veranstaltung1, visitor1);
        VeranstaltungsBesuch besuch2 = new VeranstaltungsBesuch(veranstaltung1, visitor2);
        VeranstaltungsBesuch besuch3 = new VeranstaltungsBesuch(veranstaltung2, visitor3);

        entityManager.persist(visitor1);
        entityManager.persist(visitor2);
        entityManager.persist(visitor3);

        besuch1.setEnde(new Date());
        besuch2.setEnde(new Date());

        entityManager.persist(besuch1);
        entityManager.persist(besuch2);
        entityManager.persist(besuch3);

        entityManager.flush();
    }

    @Test
    public void findeKontakteFuer() {
        befuelleDatenbank();

        Collection<VeranstaltungsBesuchDTO> kontakte = visitorRepository.findeKontakteFuer(visitor1.getEmail());
        Assertions.assertEquals(2, kontakte.size());
        Assertions.assertTrue(kontakte.stream().allMatch(besuch ->
                besuch.getBesucherEmail().equals(visitor1.getEmail()) ||
                        besuch.getBesucherEmail().equals(visitor2.getEmail())
        ));
    }
}