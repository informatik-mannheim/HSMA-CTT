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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hs_mannheim.informatik.ct.model.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.model.Visitor;
import lombok.NonNull;
import lombok.val;

@Service
public class ContactTracingService {
    @Autowired
    private List<VisitService<?>> visitServices;

    @NonNull
    public List<Contact<? extends Visit>> getVisitorContacts(@NonNull Visitor visitor) {
        val contacts = new ArrayList<Contact<?>>();
        for (val service : visitServices) {
            contacts.addAll(service.getVisitorContacts(visitor));
        }

        return contacts;
    }

    @NonNull
    public List<Contact<? extends Visit>> getVisitorContacts(@NonNull Visitor visitor, Date startDate) {
        val contacts = new ArrayList<Contact<?>>();
        for (val service : visitServices) {
            contacts.addAll(service.getVisitorContacts(visitor, startDate));
        }
        return contacts;
    }
}
