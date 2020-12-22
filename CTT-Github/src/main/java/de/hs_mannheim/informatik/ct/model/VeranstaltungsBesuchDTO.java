package de.hs_mannheim.informatik.ct.model;

import java.io.Serializable;
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
}
