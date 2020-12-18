package de.hs_mannheim.informatik.ct.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Veranstaltung {
    @Id
    @GeneratedValue
    private Long id;
	
	private String name;
	private int raumkapazitaet;
	private Date datum = new Date();
	private String angelegtVon;
	
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy="besucherEmail", cascade = {CascadeType.ALL})
	private List<VeranstaltungsBesuch> besuche = new ArrayList<>();
	
	public Veranstaltung(String name, int raumkapazitaet, Date datum, String angelegtVon) {
		this.name = name;
		this.raumkapazitaet = raumkapazitaet;
		this.datum = datum;
		this.angelegtVon = angelegtVon;
	}
	
	public Veranstaltung(Long id, String name, int raumkapazitaet, Date datum, String angelegtVon) {
		this(name, raumkapazitaet, datum, angelegtVon);
		this.id = id;
	}

	public void addBesucher(Besucher b) {
		VeranstaltungsBesuch vb = new VeranstaltungsBesuch(this, b);
		besuche.add(vb);
		b.addVeranstaltungsBesuch(vb);
	}
	
}
