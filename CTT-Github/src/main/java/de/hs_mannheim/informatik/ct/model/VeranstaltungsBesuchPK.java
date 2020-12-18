package de.hs_mannheim.informatik.ct.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class VeranstaltungsBesuchPK implements Serializable {
	private Long veranstaltungId;
	private String besucherEmail;
}

