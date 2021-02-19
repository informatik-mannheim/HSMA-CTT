package de.hs_mannheim.informatik.ct.model;

import java.util.Date;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
