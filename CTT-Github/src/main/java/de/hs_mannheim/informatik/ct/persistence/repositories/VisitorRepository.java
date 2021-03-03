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

import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;
import de.hs_mannheim.informatik.ct.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
//	@Query(value = "SELECT DISTINCT b2.Besucher_Email FROM Veranstaltungs_Besuch b1, Veranstaltungs_Besuch b2 where "
//					+ "b1.Besucher_Email = ?1 and b1.veranstaltung_Id = b2.veranstaltung_Id "
//					+ "and not b2.Besucher_Email = ?1", nativeQuery = true)
//	Collection<String> findeKontakteFuer(String email);

    // TODO: nochmal überlegen wenn jemand noch angemeldet ist, wird er in einer Veranstaltung nicht als Kontakt gefunden.
    // Das sollte aber in der Praxis kein Problem sein, da eine Verfolgung ja ohnehin erst Tage später gemacht wird.

    // Besucher der gleichen Veranstaltung in einem definierbaren Zeitintervall
    // Und auch der Gesuchte selbst wird zurückgegeben, damit man sieht, wann er in welcher Veranstaltung war
    @Query("SELECT new de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO(visitTarget, visitOther)" +
            "FROM VeranstaltungsBesuch visitTarget, " +
            "VeranstaltungsBesuch visitOther " +
            "WHERE visitTarget.visitor.email = :email " +
            "AND visitTarget.veranstaltung.id = visitOther.veranstaltung.id " +
            "AND visitTarget.wann <= visitOther.ende " +
            "AND visitOther.wann <= visitTarget.ende " +
            "ORDER BY visitOther.wann DESC")
    Collection<VeranstaltungsBesuchDTO> findContactsFor(@Param(value = "email") String email);

    Optional<Visitor> findByEmail(String email);

    /**
     * Deletes all visitors that are not listed in neither a room nor an event visit.
     */
    @Modifying
    @Query("DELETE FROM Visitor visitor " +
            "WHERE visitor NOT IN (" +
            "   SELECT roomVisit.visitor " +
            "   FROM RoomVisit roomVisit) " +
            "AND visitor NOT IN (" +
            "   SELECT visit.visitor " +
            "   FROM VeranstaltungsBesuch visit)")
    void removeVisitorsWithNoVisits();
}
