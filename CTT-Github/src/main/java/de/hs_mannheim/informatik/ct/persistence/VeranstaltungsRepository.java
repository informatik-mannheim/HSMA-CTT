package de.hs_mannheim.informatik.ct.persistence;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.hs_mannheim.informatik.ct.model.Veranstaltung;

public interface VeranstaltungsRepository extends JpaRepository<Veranstaltung, Long> {
	public Collection<Veranstaltung> findAllByOrderByDatumAsc();
	
	@Query(value = "SELECT new de.hs_mannheim.informatik.ct.model.Veranstaltung(v.id, v.name, v.raumkapazitaet, v.datum, v.angelegtVon) "
					+ "FROM de.hs_mannheim.informatik.ct.model.Veranstaltung v where v.datum >= ?1 order by datum asc")
	public Collection<Veranstaltung> findAllFromGivenDate(Date startDate);
}