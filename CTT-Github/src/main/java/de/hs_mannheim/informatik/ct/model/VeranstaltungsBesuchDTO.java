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
import java.time.Duration;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;



@AllArgsConstructor
@Data
public class VeranstaltungsBesuchDTO implements Serializable {
	private String besucherEmail;
	private long veranstaltungsId;
	private String veranstaltungsName;
	private Date timestamp;
	private Date endzeit;
	private int diffInMin;

	public VeranstaltungsBesuchDTO(EventVisit target, EventVisit other) {
		besucherEmail = target.getVisitor().getEmail();
		veranstaltungsId = target.getEvent().getId();
		veranstaltungsName = target.getEvent().getName();
		timestamp = other.getStartDate();
		endzeit = other.getEndDate();
		diffInMin = (int) Math.abs(Duration.between(
				other.getStartDate().toInstant(),
				target.getStartDate().toInstant())
				.toMinutes());
	}
}
