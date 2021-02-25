package de.hs_mannheim.informatik.ct.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Veranstaltung {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    @ManyToOne(cascade = CascadeType.ALL)
    private Room room;
    private Date datum = new Date();
    private String angelegtVon;

    public Veranstaltung(String name, Room room, Date datum, String angelegtVon) {
        this.name = name;
        this.room = room;
        this.datum = datum;
        this.angelegtVon = angelegtVon;
    }

    public int getRaumkapazitaet() {
        return room.getMaxCapacity();
    }
}
