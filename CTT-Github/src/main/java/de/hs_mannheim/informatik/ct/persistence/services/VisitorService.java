package de.hs_mannheim.informatik.ct.persistence.services;

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
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import lombok.val;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;



@Service
public class VisitorService {
    @Autowired
    private VisitorRepository visitorRepo;

    public Optional<Visitor> findVisitorByEmail(String email) {
        return visitorRepo.findByEmail(email);
    }

    @Transactional
    public Visitor findOrCreateVisitor(String email) throws InvalidEmailException {
        val visitor = findVisitorByEmail(email);
        if (visitor.isPresent()) {
            return visitor.get();
        } else if (EmailValidator.getInstance().isValid(email)) {
            return visitorRepo.save(new Visitor(email));
        } else {
            throw new InvalidEmailException();
        }
    }
}
