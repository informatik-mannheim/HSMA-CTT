package de.hs_mannheim.informatik.ct.model;

import java.io.Serializable;
import java.time.Duration;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VeranstaltungsBesuchDTO implements Serializable {
	private String besucherEmail;
	private long veranstaltungsId;
	private String veranstaltungsName;
	private Date timestamp;
	private Date endzeit;
	private int diffInMin;

	public VeranstaltungsBesuchDTO(VeranstaltungsBesuch target, VeranstaltungsBesuch other) {
		besucherEmail = target.getBesucherEmail();
		veranstaltungsId = target.getVeranstaltungId();
		veranstaltungsName = target.getVeranstaltung().getName();
		timestamp = other.getWann();
		endzeit = other.getEnde();
		diffInMin = (int) Math.abs(Duration.between(
				other.getWann().toInstant(),
				target.getWann().toInstant())
				.toMinutes());
	}
}
