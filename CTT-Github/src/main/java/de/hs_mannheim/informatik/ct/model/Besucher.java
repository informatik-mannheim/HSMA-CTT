package de.hs_mannheim.informatik.ct.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
public class Besucher {
	@Id
	private String email;
	
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "veranstaltungId", cascade = {CascadeType.ALL})
	private List<VeranstaltungsBesuch> besuche = new ArrayList<>();

	public Besucher(String email) {
		this.email = email;
	}
	
	public void addVeranstaltungsBesuch(VeranstaltungsBesuch vb) {
		besuche.add(vb);
	}
}
