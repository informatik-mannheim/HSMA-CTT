package de.hs_mannheim.informatik.ct.persistence;

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

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.Date;

public class RoomVisitHelper {
    private static final Room room = new Room("autoCheckOutTest","autoCheckOutTest", 20);

    public static RoomVisit generateVisit(Visitor visitor, LocalDateTime start, LocalDateTime end) {
        Date endDate = null;
        if (end != null) {
            endDate = TimeUtil.convertToDate(end);
        }

        return new RoomVisit(
                room,
                null,
                TimeUtil.convertToDate(start),
                endDate,
                visitor
        );
    }
}
