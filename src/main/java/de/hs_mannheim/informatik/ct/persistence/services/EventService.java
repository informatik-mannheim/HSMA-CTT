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

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import de.hs_mannheim.informatik.ct.model.Event;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.EventVisitRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.EventVisit;


@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private VisitorRepository visitorRepository;
    @Autowired
    private EventVisitRepository eventVisitRepository;
    @Autowired
    private DateTimeService dateTimeService;

    public Event saveEvent(Event entity) {
        return eventRepository.save(entity);
    }

    public Optional<Event> getEventById(long id) {
        return eventRepository.findById(id);
    }

    public EventVisit saveVisit(EventVisit eventVisit) {
        return eventVisitRepository.save(eventVisit);
    }

    public Collection<Event> getAll() {
        return eventRepository.findAllByOrderByDatumAsc();
    }

    public Collection<Event> getEventsToday() {
        long time = dateTimeService.getDateNow().getTime();
        return eventRepository.findByDatumGreaterThan(new Date(time - time % (24 * 60 * 60 * 1000)));
    }

    public int getVisitorCount(long id) {
        return eventVisitRepository.getEventVisitorCount(id);
    }
}
