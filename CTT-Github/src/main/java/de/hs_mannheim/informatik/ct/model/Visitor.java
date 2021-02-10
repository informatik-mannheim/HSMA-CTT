package de.hs_mannheim.informatik.ct.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Visitor {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	@NonNull
	private String email;

	public Visitor(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "{" +
				"email='" + email + '\'' +
				'}';
	}
}
