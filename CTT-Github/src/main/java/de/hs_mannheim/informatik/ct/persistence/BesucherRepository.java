package de.hs_mannheim.informatik.ct.persistence;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;

public interface BesucherRepository extends JpaRepository<Besucher, String> {
//	@Query(value = "SELECT DISTINCT b2.Besucher_Email FROM Veranstaltungs_Besuch b1, Veranstaltungs_Besuch b2 where "
//					+ "b1.Besucher_Email = ?1 and b1.veranstaltung_Id = b2.veranstaltung_Id "
//					+ "and not b2.Besucher_Email = ?1", nativeQuery = true)
//	Collection<String> findeKontakteFuer(String email);
	
	// Besucher der gleichen Veranstaltung in einem definierbaren Zeitintervall
	// Und auch der Gesuchte selbst wird zurückgegeben, damit man sieht, wann er in welcher Veranstaltung war
	@Query(value = "SELECT DISTINCT new de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO("
			+ "b2.besucherEmail, v.id, v.name, b2.wann, ABS(DATEDIFF('MINUTE', b1.wann, b2.wann))) "
			+ "FROM de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch b1, "
			+ "de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch b2, "
			+ "de.hs_mannheim.informatik.ct.model.Veranstaltung v where "
			+ "b1.besucherEmail = ?1 and b1.veranstaltungId = b2.veranstaltungId "
			+ "and b2.veranstaltungId = v.id and ABS(DATEDIFF('MINUTE', b1.wann, b2.wann)) <= 100 order by b2.wann desc")
	Collection<VeranstaltungsBesuchDTO> findeKontakteFuer(String email);
}