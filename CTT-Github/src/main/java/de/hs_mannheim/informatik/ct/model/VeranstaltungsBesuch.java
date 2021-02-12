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
	@JoinColumn
	@NonNull
	private Besucher besucher;

	public VeranstaltungsBesuch(Veranstaltung v, Besucher b) {
		this.veranstaltung = v;
		this.besucher = b;
	}
	
	public void setEnde(Date ende) {
		this.ende = ende;
	}

}
