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

import de.hs_mannheim.informatik.ct.model.CheckOutSource;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
public class RoomVisitHelper {
    private final Room room;

    public RoomVisit generateVisit(Visitor visitor, @NonNull LocalDateTime start, LocalDateTime end) {
        Date endDate = null;
        CheckOutSource checkOutSource = CheckOutSource.NotCheckedOut;
        if (end != null) {
            endDate = TimeUtil.convertToDate(end);
            checkOutSource = CheckOutSource.UserCheckout;
        }

        return new RoomVisit(
                room,
                null,
                TimeUtil.convertToDate(start),
                endDate,
                visitor,
                checkOutSource
        );
    }

    /**
     * Generates a list of room visits, some of which should be delete because they are after the expiration date for personal data.
     *
     * @param expiredVisitor    The visitor used for visits that should be deleted
     * @param notExpiredVisitor The vistor used for visits that are still valid
     */
    public List<RoomVisit> generateExpirationTestData(Visitor expiredVisitor, Visitor notExpiredVisitor) {
        val roomVisits = new ArrayList<RoomVisit>();

        // Absolutely not expired
        roomVisits.add(generateVisit(
                notExpiredVisitor,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()));

        // Older but also not expired
        roomVisits.add(generateVisit(
                notExpiredVisitor,
                LocalDateTime.now().minusHours(1).minusDays(25),
                LocalDateTime.now().minusDays(25)));

        // Definitely expired
        roomVisits.add(generateVisit(
                expiredVisitor,
                LocalDateTime.now().minusHours(1).minusMonths(2),
                LocalDateTime.now().minusMonths(2)));

        // Just expired
        roomVisits.add(generateVisit(
                expiredVisitor,
                LocalDateTime.now().minusHours(1).minusWeeks(4).minusDays(1),
                LocalDateTime.now().minusWeeks(4).minusDays(1)));

        return roomVisits;
    }
}
