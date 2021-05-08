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
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.naming.InvalidNameException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
public class VisitorServiceTest {
    @TestConfiguration
    static class VisitorServiceTestConfig {
        @Bean
        public VisitorService visitorService() {
            return new VisitorService();
        }
    }

    @Autowired
    private VisitorService visitorService;

    @MockBean
    private VisitorRepository visitorRepository;

    @Test
    void createVisitorsCheckEmail() {
        // Create new visitors
        Mockito.when(visitorRepository.findByEmail(any(String.class)))
                .thenReturn(Optional.empty());

        Mockito.when(visitorRepository.save(any(Visitor.class)))
                .thenAnswer(visitor -> visitor.getArgument(0, Visitor.class));

        // Empty email
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("",null, null, null));

        // Incomplete email
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("13337@",null, null, null));
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("12",null, null, null));
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("12@stud.hs",null, null, null));

        // External Guest required mail, name and (number or address)
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("","CoolName", "Number is a String", "null"));
        Assertions.assertThrows(InvalidExternalUserdataException.class, () -> visitorService.findOrCreateVisitor("myEmail@gmx.de",null, "123456 call me", null));
        Assertions.assertThrows(InvalidExternalUserdataException.class, () -> visitorService.findOrCreateVisitor("imagine@web.de","Dragons", null, null));
        try {
            //Valid external emails
            visitorService.findOrCreateVisitor("test@gmx.de","Tester1", "123", null);
            visitorService.findOrCreateVisitor("test_test@gmail.com","Tester2", "345", null);
            visitorService.findOrCreateVisitor("t.est@fc-md.umd.edu","Tester3", "234", null);
            // Valid internal emails
            visitorService.findOrCreateVisitor("13337@stud.hs-mannheim.de",null, null, null);
            visitorService.findOrCreateVisitor("p.prof@hs-mannheim.de",null, null, null);
        } catch (InvalidEmailException | InvalidExternalUserdataException e) {
            fail("Valid email caused exception");
        }
    }

    /**
     * There was a bug that caused the findOrCreateVisitor method to save the visitor even if it was found. This causes
     * a constraint violation, Therefore ensure that save is ONLY called if the visitor wasn't found.
     */
    @Test
    void finOrCreateNoDuplicateSave() throws InvalidEmailException, InvalidExternalUserdataException {
        val email = "1234@stud.hs-mannheim.de";
        val visitor = new Visitor();
        // The visitor doesn't exist yet

        doReturn(Optional.empty()).when(visitorRepository).findByEmail(email);
        visitorService.findOrCreateVisitor(email,null, null, null);

        // The visitor exists now
        doReturn(Optional.of(visitor)).when(visitorRepository).findByEmail(email);
        Assertions.assertTrue(visitorService.findVisitorByEmail(email).isPresent());
        // This should return the visitor without creating a new one
        visitorService.findOrCreateVisitor(email,null, null, null);

        // Finally ensure that save was called exactly once
        verify(
                visitorRepository,
                Mockito.times(1))
                .save(any(Visitor.class));
    }
}
