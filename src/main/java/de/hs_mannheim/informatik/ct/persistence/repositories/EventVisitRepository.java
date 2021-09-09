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

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.model.EventVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;

public interface EventVisitRepository extends JpaRepository<EventVisit, Long> {
    @Modifying
    @Transactional
    void deleteByEndDateBefore(Date endDate);

    @Query("SELECT COUNT(*) " +
            "FROM EventVisit visit " +
            "WHERE visit.event.id = ?1 AND endDate IS NULL")
    int getEventVisitorCount(long eventId);

    @Query("SELECT eventVisit " +
            "FROM EventVisit eventVisit " +
            "WHERE eventVisit.visitor.email = :visitorEmail and eventVisit.endDate is null")
    List<EventVisit> getNotSignedOut(@Param(value = "visitorEmail") String visitorEmail);

    @Query("SELECT NEW de.hs_mannheim.informatik.ct.model.Contact(visitTarget, visitOther) " +
            "FROM EventVisit visitTarget," +
            "EventVisit visitOther " +
            "WHERE visitTarget.visitor = :visitor AND " +
            "visitTarget.visitor != visitOther.visitor AND " +
            "visitTarget.event = visitOther.event AND " +
            "visitTarget.startDate <= visitOther.endDate AND " +
            "visitOther.startDate <= visitTarget.endDate " +
            "ORDER BY visitTarget.startDate")
    List<Contact<EventVisit>> findVisitsWithContact(@Param(value = "visitor") Visitor visitor);
}
