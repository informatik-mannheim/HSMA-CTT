package de.hs_mannheim.informatik.ct.persistence.services;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsBesuchRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;

// 	 	<Corona Tracking Tool der Hochschule Mannheim>
//		Copyright (C) <2021>  <Hochschule Mannheim>
//
//		This program is free software: you can redistribute it and/or modify
//		it under the terms of the GNU Affero General Public License as published by
//		the Free Software Foundation, either version 3 of the License, or
//		(at your option) any later version.
//
//		This program is distributed in the hope that it will be useful,
//		but WITHOUT ANY WARRANTY; without even the implied warranty of
//		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//		GNU Affero General Public License for more details.
//
//		You should have received a copy of the GNU Affero General Public License
//		along with this program.  If not, see <https://www.gnu.org/licenses/>.

@Service
public class VeranstaltungsService {
	@Autowired
	private VeranstaltungsRepository repoV;
	@Autowired
	private VisitorRepository repoB;
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

	public Collection<VeranstaltungsBesuchDTO> findeKontakteFuer(String email) {
		return repoB.findContactsFor(email);
	}

	public Collection<Veranstaltung> findeAlleVeranstaltungen() {
//		return repoV.findAll();
		return repoV.findAllByOrderByDatumAsc();
	}
	
	public Collection<Veranstaltung> findeAlleHeutigenVeranstaltungen() {
		Long time = new Date().getTime();
		return repoV.findByDatumGreaterThan(new Date(time - time % (24 * 60 * 60 * 1000)));
	}
	
	public int getBesucherAnzahl(long id) {
		return repoVB.getVisitorCountById(id);
	}
}
