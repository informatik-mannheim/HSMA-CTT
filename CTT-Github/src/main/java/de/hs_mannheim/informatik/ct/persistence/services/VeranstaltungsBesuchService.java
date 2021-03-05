package de.hs_mannheim.informatik.ct.persistence.services;

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

import com.sun.istack.NotNull;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsBesuchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;



@Service
public class VeranstaltungsBesuchService {

    @Autowired
    private VeranstaltungsBesuchRepository repoVeranstaltungsBesuche;

    private void abmelden(Visitor visitor, Veranstaltung veranstaltung, @NotNull Date ende) {
        repoVeranstaltungsBesuche.besucherAbmelden(visitor.getEmail(), ende);
    }

    /**
     * Holt alle Besuche eines Besuchers, bei denen er sich noch nicht abgemeldet hat. Normalerweise sollte das nur höchstens einer sein.
     * @param visitor Besucher für den nach nicht abgemeldeten Besuchen gesucht wird.
     * @return Alle nicht abgemeldeten Besuche.
     */
    public List<VeranstaltungsBesuch> getNichtAbgemeldeteBesuche(Visitor visitor) {
        return repoVeranstaltungsBesuche.nichtAbgemeldeteBesuche(visitor.getEmail());
    }

    /**
     * Meldet den Besucher aus allen angemeldeten Veranstaltungen ab.
     * @param visitor Besucher für den nach nicht abgemeldeten Besuchen gesucht wird.
     * @return Alle nicht abgemeldeten Besuche.
     */
    @Transactional
    public List<VeranstaltungsBesuch> besucherAbmelden(Visitor visitor, Date ende) {
        List<VeranstaltungsBesuch> veranstaltungsBesuche = getNichtAbgemeldeteBesuche(visitor);
        for (VeranstaltungsBesuch besuch: veranstaltungsBesuche) {
            abmelden(visitor, besuch.getVeranstaltung(), ende);
        }

        return veranstaltungsBesuche;
    }
}
