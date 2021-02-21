package de.hs_mannheim.informatik.ct.persistence.services;

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
