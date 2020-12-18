package de.hs_mannheim.informatik.ct.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchPK;

public interface VeranstaltungsBesuchRepository extends JpaRepository<VeranstaltungsBesuch, VeranstaltungsBesuchPK> {
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM veranstaltungs_besuch WHERE DATEDIFF('DAY', timestamp, NOW()) >= 1", nativeQuery = true)
	void loescheAlteBesuche();
	// TODO Hier muss DAY im Produktivbetrieb auf MONTH gesetzt werden bzw. auf 4 WEEK

	@Query(value = "SELECT COUNT(*) from veranstaltungs_besuch where veranstaltung_id = ?1", nativeQuery = true)
	int getVisitorCountById(long id);
	
//	@Query(value = "SELECT DATEDIFF('WEEK', timestamp, NOW()) FROM veranstaltungs_besuch", nativeQuery = true)
//	List<Integer> getBesuchsAlter();
}