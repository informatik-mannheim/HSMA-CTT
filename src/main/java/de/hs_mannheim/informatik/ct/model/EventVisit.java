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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor
public class EventVisit implements Visit {
    @EmbeddedId
    @Getter(value = AccessLevel.NONE)
    private PrimaryKey id;

    @Column(updatable = false)
    private Date startDate;

    @Column
    private Date endDate;

    @ManyToOne
    @MapsId("eventId")
    @NonNull
    private Event event;

    @ManyToOne
    @MapsId("visitorId")
    @NonNull
    private Visitor visitor;

    public EventVisit(Event event, Visitor visitor, Date startDate) {
        this.event = event;
        this.visitor = visitor;
        this.startDate = startDate;

        this.id = new PrimaryKey(event, visitor);
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String getLocationName() {
        return event.getName();
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        PrimaryKey(Event event, Visitor visitor) {
            this.eventId = event.getId();
            this.visitorId = visitor.getId();
        }

        @NonNull
        @Column(nullable = false)
        private Long eventId;

        @NonNull
        @Column(nullable = false)
        private Long visitorId;
    }
}
