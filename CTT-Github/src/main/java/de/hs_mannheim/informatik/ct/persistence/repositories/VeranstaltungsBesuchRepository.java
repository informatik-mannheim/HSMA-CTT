package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface VeranstaltungsBesuchRepository extends JpaRepository<VeranstaltungsBesuch, VeranstaltungsBesuchPK> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM veranstaltungs_besuch WHERE DATEDIFF('DAY', timestamp, NOW()) >= 1", nativeQuery = true)
    void loescheAlteBesuche();
    // TODO Hier muss DAY im Produktivbetrieb auf MONTH gesetzt werden bzw. auf 4 WEEK

    @Query(value = "SELECT COUNT(*) from veranstaltungs_besuch where veranstaltung_id = ?1", nativeQuery = true)
    int getVisitorCountById(long id);

    @Modifying
    @Transactional
    @Query("UPDATE VeranstaltungsBesuch vb " +
            "SET vb.ende = :ende " +
            "WHERE vb.besucherEmail =:besucherEmail AND vb.veranstaltungId = :veranstaltungId")
    void besucherAbmelden(
            @Param(value = "besucherEmail") String besucherEmail,
            @Param(value = "veranstaltungId") Long veranstaltungId,
            @Param(value = "ende") Date ende);

    @Query("SELECT vb " +
            "FROM VeranstaltungsBesuch vb " +
            "WHERE vb.besucherEmail = :besucherEmail and vb.ende is null")
    List<VeranstaltungsBesuch> nichtAbgemeldeteBesuche(@Param(value = "besucherEmail") String besucherEmail);
}