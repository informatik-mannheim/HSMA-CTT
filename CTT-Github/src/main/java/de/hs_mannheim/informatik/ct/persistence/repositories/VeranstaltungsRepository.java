package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface VeranstaltungsRepository extends JpaRepository<Veranstaltung, Long> {
	List<Veranstaltung> findAllByOrderByDatumAsc();

	List<Veranstaltung> findByDatumGreaterThan(Date startDate);

	List<Veranstaltung> findByRoom(Room room);
}