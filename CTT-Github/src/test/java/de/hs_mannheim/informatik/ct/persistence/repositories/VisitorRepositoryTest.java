package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.model.*;
import org.hamcrest.Matchers;
import org.hamcrest.junit.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class VisitorRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Visitor visitor1 = new Visitor("12345@stud.hs-mannheim.de");
    private final Visitor visitor2 = new Visitor("13337@stud.hs-mannheim.de");
    private final Visitor visitor3 = new Visitor("77777@stud.hs-mannheim.de");

    //visitor for encryption testing
    private final String decryptedEmail = "987654321@stud.hs-mannheim.de";
    private final Visitor visitor4 = new Visitor(decryptedEmail);

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
        entityManager.persist(visitor4);

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

        Collection<VeranstaltungsBesuchDTO> kontakte = visitorRepository.findContactsFor(visitor1.getEmail());
        Assertions.assertEquals(2, kontakte.size());
        Assertions.assertTrue(kontakte.stream().allMatch(besuch ->
                besuch.getBesucherEmail().equals(visitor1.getEmail()) ||
                        besuch.getBesucherEmail().equals(visitor2.getEmail())
        ));
    }

    /**
     * Uses the repository to fetch a visitor and checks if the email gets correctly decrypted
     */
    @Test
    public void readDecryptedViaRepo() {
        befuelleDatenbank();

        Optional<Visitor> visitorOptional = visitorRepository.findByEmail(decryptedEmail);
        Assertions.assertEquals(decryptedEmail, visitorOptional.get().getEmail());
    }

    /**
     * Gets all rows from the table visitor directly from the database and checks if the email can be read in clear text (it shouldn't)
     */
    @Test
    public void readEncryptedViaJDBC() {
        befuelleDatenbank();

        String query = "SELECT * FROM  visitor";

        jdbcTemplate.query(query, rs -> {
            ArrayList<String> resultsList = new ArrayList<>();
            while (rs.next()) {
                resultsList.add(rs.getString("EMAIL"));
            }
            MatcherAssert.assertThat(resultsList.size(), Matchers.greaterThan(0));
            Assertions.assertFalse(resultsList.contains(decryptedEmail));
        });


    }


}