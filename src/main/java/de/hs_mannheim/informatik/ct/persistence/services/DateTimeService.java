/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.persistence.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class DateTimeService {
    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public Date getDateNow() {
        return Date.from(getNow().atZone(ZoneId.systemDefault()).toInstant());
    }

    public Date getDate(int year, int month, int dayOfMonth, int hour, int minute){
        return Date.from(LocalDateTime.of(year, month, dayOfMonth, hour, minute).atZone(ZoneId.systemDefault()).toInstant());
    }
}
