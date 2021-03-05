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