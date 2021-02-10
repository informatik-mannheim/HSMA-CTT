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
@NoArgsConstructor
public class Visitor {
	@Id
	private String email;

	public Visitor(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "Besucher{" +
				"email='" + email + '\'' +
				'}';
	}
}
