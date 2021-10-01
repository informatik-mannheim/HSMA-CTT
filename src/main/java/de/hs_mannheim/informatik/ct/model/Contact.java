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

package de.hs_mannheim.informatik.ct.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Contact<T extends Visit> {
    private T targetVisit;
    private T contactVisit;

    /**
     * 
     * @return the infected person
     */
    public Visitor getTarget() {
        return targetVisit.getVisitor();
    }

    /**
     *
     * @return likely contact of infected person
     */
    public Visitor getContact() { 
        return contactVisit.getVisitor(); 
    }

    public String getContactLocation() {
        assert targetVisit.getLocationName().equals(contactVisit.getLocationName());
        return targetVisit.getLocationName();
    }
    
}
