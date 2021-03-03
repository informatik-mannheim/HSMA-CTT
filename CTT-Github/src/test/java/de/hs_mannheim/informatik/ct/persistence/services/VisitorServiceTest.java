package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
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
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor(""));

        // Incomplete email
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("13337@"));
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("12"));
        Assertions.assertThrows(InvalidEmailException.class, () -> visitorService.findOrCreateVisitor("12@stud.hs"));

        try {
            //Valid external emails
            visitorService.findOrCreateVisitor("test@gmx.de");
            visitorService.findOrCreateVisitor("test_test@gmail.com");
            visitorService.findOrCreateVisitor("t.est@fc-md.umd.edu");
            // Valid internal emails
            visitorService.findOrCreateVisitor("13337@stud.hs-mannheim.de");
            visitorService.findOrCreateVisitor("p.prof@hs-mannheim.de");
        } catch (InvalidEmailException e) {
            fail("Valid email caused exception");
        }
    }

    /**
     * There was a bug that caused the findOrCreateVisitor method to save the visitor even if it was found. This causes
     * a constraint violation, Therefore ensure that save is ONLY called if the visitor wasn't found.
     */
    @Test
    void finOrCreateNoDuplicateSave() throws InvalidEmailException {
        val email = "1234@stud.hs-mannheim.de";
        val visitor = new Visitor();
        // The visitor doesn't exist yet

        doReturn(Optional.empty()).when(visitorRepository).findByEmail(email);
        visitorService.findOrCreateVisitor(email);

        // The visitor exists now
        doReturn(Optional.of(visitor)).when(visitorRepository).findByEmail(email);
        Assertions.assertTrue(visitorService.findVisitorByEmail(email).isPresent());
        // This should return the visitor without creating a new one
        visitorService.findOrCreateVisitor(email);

        // Finally ensure that save was called exactly once
        verify(
                visitorRepository,
                Mockito.times(1))
                .save(any(Visitor.class));
    }
}
