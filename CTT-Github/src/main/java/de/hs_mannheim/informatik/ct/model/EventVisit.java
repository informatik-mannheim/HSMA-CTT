package de.hs_mannheim.informatik.ct.model;

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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import lombok.*;


@Entity
@Getter
@NoArgsConstructor
public class EventVisit {
	@EmbeddedId
	@Getter(value = AccessLevel.NONE)
	private PrimaryKey id;

	@Column(updatable = false)
	private final Date startDate = new Date();

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

	public EventVisit(Event event, Visitor visitor) {
		this.event = event;
		this.visitor = visitor;

		this.id = new PrimaryKey(event, visitor);
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
