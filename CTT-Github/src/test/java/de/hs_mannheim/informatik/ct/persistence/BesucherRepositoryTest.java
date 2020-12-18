package de.hs_mannheim.informatik.ct.persistence;

import java.util.Collection;
import java.util.Date;

import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class BesucherRepositoryTest extends TestCase {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BesucherRepository besucherRepository;

    private final Besucher besucher1 = new Besucher("12345@stud.hs-mannheim.de");
    private final Besucher besucher2 = new Besucher("13337@stud.hs-mannheim.de");
    private final Besucher besucher3 = new Besucher("77777@stud.hs-mannheim.de");
    
    private void befuelleDatenbank() {
        Veranstaltung veranstaltung1 = new Veranstaltung("PR1", 2, new Date(), "Herr Müller");
        Veranstaltung veranstaltung2 = new Veranstaltung("PR2", 20, new Date(), "Frau Meier");

        entityManager.persist(veranstaltung1);
        entityManager.persist(veranstaltung2);

        VeranstaltungsBesuch besuch1 = new VeranstaltungsBesuch(veranstaltung1, besucher1);
        VeranstaltungsBesuch besuch2 = new VeranstaltungsBesuch(veranstaltung1, besucher2);
        VeranstaltungsBesuch besuch3 = new VeranstaltungsBesuch(veranstaltung2, besucher3);

        entityManager.persist(besucher1);
        entityManager.persist(besucher2);
        entityManager.persist(besucher3);
        entityManager.persist(besuch1);
        entityManager.persist(besuch2);
        entityManager.persist(besuch3);
        entityManager.flush();
    }

    @Test
    public void findeKontakteFuer() {
    	befuelleDatenbank();
    	
        Collection<VeranstaltungsBesuchDTO> kontakte = besucherRepository.findeKontakteFuer(besucher1.getEmail());
        Assertions.assertEquals(kontakte.size(), 2);
        Assertions.assertTrue(kontakte.stream().allMatch(besuch ->
                besuch.getBesucherEmail().equals(besucher1.getEmail()) ||
                        besuch.getBesucherEmail().equals(besucher2.getEmail())
        ));
    }
}