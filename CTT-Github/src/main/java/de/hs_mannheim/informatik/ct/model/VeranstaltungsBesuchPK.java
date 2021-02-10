package de.hs_mannheim.informatik.ct.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class VeranstaltungsBesuchPK implements Serializable {
	@Column(name = "veranstaltungs_id", nullable = false)
	private Long veranstaltungId;
	@Column(name = "visitor_email", nullable = false)
	private String visitorEmail;
}

