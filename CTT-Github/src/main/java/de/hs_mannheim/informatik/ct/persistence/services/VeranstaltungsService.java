package de.hs_mannheim.informatik.ct.persistence.services;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import de.hs_mannheim.informatik.ct.persistence.repositories.BesucherRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsBesuchRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;

@Service
public class VeranstaltungsService {
	@Autowired
	private VeranstaltungsRepository repoV;
	@Autowired
	private BesucherRepository repoB;
	@Autowired
	private VeranstaltungsBesuchRepository repoVB;

	public Veranstaltung speichereVeranstaltung(Veranstaltung entity) {
		return repoV.save(entity);
	}
	
	public Optional<Veranstaltung> getVeranstaltungById(long id) {
		return repoV.findById(id);
	}
	
	public VeranstaltungsBesuch speichereBesuch(VeranstaltungsBesuch vb) {
		return repoVB.save(vb);
	}

	public Besucher getBesucherByEmail(String email) {
		Optional<Besucher> opt = repoB.findById(email);
		
		if (!opt.isPresent()) 
			return null; 
		
		return repoB.findById(email).get();
	}

	public Besucher speichereBesucher(Besucher b) {
		return repoB.save(b);
	}

	public Collection<VeranstaltungsBesuchDTO> findeKontakteFuer(String email) {
		return repoB.findeKontakteFuer(email);
	}

	public Collection<Veranstaltung> findeAlleVeranstaltungen() {
//		return repoV.findAll();
		return repoV.findAllByOrderByDatumAsc();
	}
	
	public Collection<Veranstaltung> findeAlleHeutigenVeranstaltungen() {
		Long time = new Date().getTime();
		return repoV.findByDatumGreaterThan(new Date(time - time % (24 * 60 * 60 * 1000)));
	}
	
//	@Scheduled(cron = "0 * * * * *")	// jede Minute
	@Scheduled(cron = "0 55 2 * * *")	// um 2:55 Uhr
	public void loescheAlteBesuche() {
		repoVB.loescheAlteBesuche();
	}
	
	public int getBesucherAnzahl(long id) {
		return repoVB.getVisitorCountById(id);
	}
}
