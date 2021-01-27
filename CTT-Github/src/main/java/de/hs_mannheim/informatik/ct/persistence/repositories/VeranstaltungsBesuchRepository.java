package de.hs_mannheim.informatik.ct.persistence.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchPK;

public interface VeranstaltungsBesuchRepository extends JpaRepository<VeranstaltungsBesuch, VeranstaltungsBesuchPK> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM veranstaltungs_besuch WHERE DATEDIFF('WEEK', timestamp, NOW()) >= 4", nativeQuery = true)
    void loescheAlteBesuche();
    // TODO Hier kann im Testbetrieb auf DAY und im Produktivbetrieb auf MONTH gesetzt werden, bzw. auf 4 WEEK

    @Query(value = "SELECT COUNT(*) from veranstaltungs_besuch where veranstaltung_id = ?1 and ende is null", nativeQuery = true)
    int getVisitorCountById(long id);

    @Modifying
    @Transactional
    @Query("UPDATE VeranstaltungsBesuch vb " +
            "SET vb.ende = :ende " +
            "WHERE vb.besucherEmail =:besucherEmail AND vb.ende is null")
    void besucherAbmelden(@Param(value = "besucherEmail") String besucherEmail, @Param(value = "ende") Date ende);

    @Query("SELECT vb " +
            "FROM VeranstaltungsBesuch vb " +
            "WHERE vb.besucherEmail = :besucherEmail and vb.ende is null")
    List<VeranstaltungsBesuch> nichtAbgemeldeteBesuche(@Param(value = "besucherEmail") String besucherEmail);
}