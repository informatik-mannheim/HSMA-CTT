package de.hs_mannheim.informatik.ct.persistence.repositories;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */ 

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;



public interface VeranstaltungsBesuchRepository extends JpaRepository<VeranstaltungsBesuch, Long> {

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
            "WHERE vb.visitor.email =:besucherEmail AND vb.ende is null")
    void besucherAbmelden(@Param(value = "besucherEmail") String besucherEmail, @Param(value = "ende") Date ende);

    @Query("SELECT vb " +
            "FROM VeranstaltungsBesuch vb " +
            "WHERE vb.visitor.email = :besucherEmail and vb.ende is null")
    List<VeranstaltungsBesuch> nichtAbgemeldeteBesuche(@Param(value = "besucherEmail") String besucherEmail);
}