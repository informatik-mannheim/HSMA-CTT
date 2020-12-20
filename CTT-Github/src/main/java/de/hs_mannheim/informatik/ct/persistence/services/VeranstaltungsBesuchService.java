package de.hs_mannheim.informatik.ct.persistence.services;

import com.sun.istack.NotNull;
import de.hs_mannheim.informatik.ct.model.Besucher;
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

    private void abmelden(Besucher besucher, Veranstaltung veranstaltung, @NotNull Date ende) {
        repoVeranstaltungsBesuche.besucherAbmelden(besucher.getEmail(), ende);
    }

    /**
     * Holt alle Besuche eines Besuchers, bei denen er sich noch nicht abgemeldet hat. Normalerweise sollte das nur höchstens einer sein.
     * @param besucher Besucher für den nach nicht abgemeldeten Besuchen gesucht wird.
     * @return Alle nicht abgemeldeten Besuche.
     */
    public List<VeranstaltungsBesuch> getNichtAbgemeldeteBesuche(Besucher besucher) {
        return repoVeranstaltungsBesuche.nichtAbgemeldeteBesuche(besucher.getEmail());
    }

    /**
     * Meldet den Besucher aus allen angemeldeten Veranstaltungen ab.
     * @param besucher Besucher für den nach nicht abgemeldeten Besuchen gesucht wird.
     * @return Alle nicht abgemeldeten Besuche.
     */
    @Transactional
    public List<VeranstaltungsBesuch> besucherAbmelden(Besucher besucher, Date ende) {
        List<VeranstaltungsBesuch> veranstaltungsBesuche = getNichtAbgemeldeteBesuche(besucher);
        for (VeranstaltungsBesuch besuch: veranstaltungsBesuche) {
            abmelden(besucher, besuch.getVeranstaltung(), ende);
        }

        return veranstaltungsBesuche;
    }
}
