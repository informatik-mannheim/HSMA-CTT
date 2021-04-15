package de.hs_mannheim.informatik.ct.persistence.services;

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

import com.sun.istack.NotNull;
import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.model.EventVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Event;
import de.hs_mannheim.informatik.ct.persistence.repositories.EventVisitRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.List;

import static de.hs_mannheim.informatik.ct.util.TimeUtil.convertToDate;


@Service
public class EventVisitService implements VisitService<EventVisit> {

    @Autowired
    private EventVisitRepository eventVisitRepository;

    @Transactional
    private void signOut(Visitor visitor, Event event, @NotNull Date end) {
        val visits = eventVisitRepository.getNotSignedOut(visitor.getEmail());
        for (val visit :
                visits) {
            visit.setEndDate(end);
        }

        eventVisitRepository.saveAll(visits);
    }

    /**
     * Holt alle Besuche eines Besuchers, bei denen er sich noch nicht abgemeldet hat. Normalerweise sollte das nur höchstens einer sein.
     * @param visitor Besucher für den nach nicht abgemeldeten Besuchen gesucht wird.
     * @return Alle nicht abgemeldeten Besuche.
     */
    public List<EventVisit> getNotSignedOutVisits(Visitor visitor) {
        return eventVisitRepository.getNotSignedOut(visitor.getEmail());
    }

    /**
     * Meldet den Besucher aus allen angemeldeten Veranstaltungen ab.
     * @param visitor Besucher für den nach nicht abgemeldeten Besuchen gesucht wird.
     * @return Alle nicht abgemeldeten Besuche.
     */
    @Transactional
    public List<EventVisit> signOutVisitor(Visitor visitor, Date end) {
        List<EventVisit> eventVisits = getNotSignedOutVisits(visitor);
        for (EventVisit visit: eventVisits) {
            signOut(visitor, visit.getEvent(), end);
        }

        return eventVisits;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteExpiredRecords(Period recordLifeTime) {
        val oldestAllowedRecord = LocalDateTime.now().minus(recordLifeTime);
        eventVisitRepository.deleteByEndDateBefore(convertToDate(oldestAllowedRecord));
    }

    @Override
    public List<Contact<EventVisit>> getVisitorContacts(@NonNull Visitor visitor) {
        return eventVisitRepository.findVisitsWithContact(visitor);
    }
}
