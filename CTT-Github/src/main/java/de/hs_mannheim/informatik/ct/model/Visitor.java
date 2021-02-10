package de.hs_mannheim.informatik.ct.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = @Index(columnList = "email"))
public class Visitor {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true, nullable = false)
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
