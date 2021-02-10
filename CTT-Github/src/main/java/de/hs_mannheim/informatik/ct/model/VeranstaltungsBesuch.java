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
	@MapsId("visitorEmail")
	@NonNull
	private Visitor visitor;

	public VeranstaltungsBesuch(Veranstaltung v, Visitor b) {
		this.veranstaltung = v;
		this.visitor = b;

		this.id = new VeranstaltungsBesuchPK(v.getId(), b.getEmail());
	}
	
	public void setEnde(Date ende) {
		this.ende = ende;
	}
}
