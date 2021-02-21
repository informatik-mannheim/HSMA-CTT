package de.hs_mannheim.informatik.ct.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class VeranstaltungsBesuchPK implements Serializable {
	VeranstaltungsBesuchPK(Veranstaltung veranstaltung, Visitor visitor) {
		this.veranstaltungsId = veranstaltung.getId();
		this.visitorId = visitor.getId();
	}

	@NonNull
	@Column(name = "veranstaltungs_id", nullable = false)
	private Long veranstaltungsId;

	@NonNull
	@Column(name = "visitor_id", nullable = false)
	private Long visitorId;
}

