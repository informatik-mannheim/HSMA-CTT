package de.hs_mannheim.informatik.ct.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class VeranstaltungsBesuchPK implements Serializable {
	@NonNull
	@Column(name = "veranstaltungs_id", nullable = false)
	private Long veranstaltungsId;

	@NonNull
	@Column(name = "visitor_email", nullable = false)
	private String visitorEmail;
}

