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

import de.hs_mannheim.informatik.ct.model.*;
import org.hamcrest.Matchers;
import org.hamcrest.junit.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        Room room = new Room("A008","A", 2);
        Event event1 = new Event("PR1", room, new Date(), "Herr Müller");
        Event event2 = new Event("PR2", room, new Date(), "Frau Meier");

        entityManager.persist(event1);
        entityManager.persist(event2);

        EventVisit besuch1 = new EventVisit(event1, visitor1, new Date());
        EventVisit besuch2 = new EventVisit(event1, visitor2, new Date());
        EventVisit besuch3 = new EventVisit(event2, visitor3, new Date());

        entityManager.persist(visitor1);
        entityManager.persist(visitor2);
        entityManager.persist(visitor3);
        entityManager.persist(visitor4);

        besuch1.setEndDate(new Date());
        besuch2.setEndDate(new Date());

        entityManager.persist(besuch1);
        entityManager.persist(besuch2);
        entityManager.persist(besuch3);

        entityManager.flush();
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