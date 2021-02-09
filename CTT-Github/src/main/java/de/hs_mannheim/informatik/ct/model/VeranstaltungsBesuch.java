package de.hs_mannheim.informatik.ct.model;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor
public class VeranstaltungsBesuch {
	@Id
	@GeneratedValue
	private long id;

	@Column(name="timestamp", updatable = false)
	private Date wann = new Date();

	@Column
	private Date ende;

	@ManyToOne
	@JoinColumn
	@NonNull
	private Veranstaltung veranstaltung;

	@ManyToOne
	@MapsId("besucherEmail")
	private Visitor visitor;

	public VeranstaltungsBesuch(Veranstaltung v, Visitor b) {
		this.veranstaltung = v;
		this.visitor = b;
	}
	
	public void setEnde(Date ende) {
		this.ende = ende;
	}

}
