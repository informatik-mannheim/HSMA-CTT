package de.hs_mannheim.informatik.ct.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import lombok.NoArgsConstructor;

@Entity
@IdClass(VeranstaltungsBesuchPK.class)
@NoArgsConstructor
public class VeranstaltungsBesuch {

	@Column(name="timestamp", updatable = false)
	private Date wann = new Date();

	@Id
	@Column(name = "veranstaltung_id")
	private long veranstaltungId;
	
	@ManyToOne
	@MapsId("veranstaltungId")
	private Veranstaltung veranstaltung;

	@Id
	@Column(name = "besucher_email")
	private String besucherEmail;
	
	@ManyToOne
	@MapsId("besucherEmail")
	private Besucher besucher;

	public VeranstaltungsBesuch(Veranstaltung v, Besucher b) {
		this.veranstaltung = v;
		this.besucher = b;
		
		this.veranstaltungId = v.getId();
		this.besucherEmail = b.getEmail();
	}

}
