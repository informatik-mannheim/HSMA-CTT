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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import de.hs_mannheim.informatik.ct.model.Visitor;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
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
            "SELECT visit.visitor " +
            "FROM EventVisit visit)")
    void removeVisitorsWithNoVisits();
}
