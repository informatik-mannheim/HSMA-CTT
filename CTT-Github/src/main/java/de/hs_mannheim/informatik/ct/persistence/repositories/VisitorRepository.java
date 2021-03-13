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
    // TODO: nochmal überlegen wenn jemand noch angemeldet ist, wird er in einer Event nicht als Kontakt gefunden.
    // Das sollte aber in der Praxis kein Problem sein, da eine Verfolgung ja ohnehin erst Tage später gemacht wird.

    // Besucher der gleichen Event in einem definierbaren Zeitintervall
    // Und auch der Gesuchte selbst wird zurückgegeben, damit man sieht, wann er in welcher Event war
    @Query("SELECT new de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO(visitTarget, visitOther)" +
            "FROM EventVisit visitTarget, " +
            "EventVisit visitOther " +
            "WHERE visitTarget.visitor.email = :email " +
            "AND visitTarget.event.id = visitOther.event.id " +
            "AND visitTarget.startDate <= visitOther.endDate " +
            "AND visitOther.startDate <= visitTarget.endDate " +
            "ORDER BY visitOther.startDate DESC")
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
            "   FROM EventVisit visit)")
    void removeVisitorsWithNoVisits();
}
