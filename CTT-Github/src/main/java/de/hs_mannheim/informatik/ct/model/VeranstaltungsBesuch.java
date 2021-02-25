package de.hs_mannheim.informatik.ct.model;

import java.util.Date;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

// 	 	<Corona Tracking Tool der Hochschule Mannheim>
//		Copyright (C) <2021>  <Hochschule Mannheim>
//
//		This program is free software: you can redistribute it and/or modify
//		it under the terms of the GNU Affero General Public License as published by
//		the Free Software Foundation, either version 3 of the License, or
//		(at your option) any later version.
//
//		This program is distributed in the hope that it will be useful,
//		but WITHOUT ANY WARRANTY; without even the implied warranty of
//		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//		GNU Affero General Public License for more details.
//
//		You should have received a copy of the GNU Affero General Public License
//		along with this program.  If not, see <https://www.gnu.org/licenses/>.

@Entity
@Getter
@NoArgsConstructor
public class VeranstaltungsBesuch {
	@EmbeddedId
	@Getter(value = AccessLevel.NONE)
	private VeranstaltungsBesuchPK id;

	@Column(name="timestamp", updatable = false)
	private Date wann = new Date();

	@Column
	private Date ende;

	@ManyToOne
	@MapsId("veranstaltungsId")
	@NonNull
	private Veranstaltung veranstaltung;

	@ManyToOne
	@MapsId("visitorId")
	@NonNull
	private Visitor visitor;

	public VeranstaltungsBesuch(Veranstaltung veranstaltung, Visitor visitor) {
		this.veranstaltung = veranstaltung;
		this.visitor = visitor;

		this.id = new VeranstaltungsBesuchPK(veranstaltung, visitor);
	}
	
	public void setEnde(Date ende) {
		this.ende = ende;
	}
}
