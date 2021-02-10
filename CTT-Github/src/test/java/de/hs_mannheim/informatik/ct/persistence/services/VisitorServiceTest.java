package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
class VisitorServiceTest {
    @TestConfiguration
    static class VisitorServiceTestContextConfig {
        @Bean
        public VisitorService service() {
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
        Assertions.assertThrows(InvalidEmailException.class,() -> visitorService.findOrCreateVisitor(""));

        // Incomplete email
        Assertions.assertThrows(InvalidEmailException.class,() -> visitorService.findOrCreateVisitor("13337@"));
        Assertions.assertThrows(InvalidEmailException.class,() -> visitorService.findOrCreateVisitor("12"));
        Assertions.assertThrows(InvalidEmailException.class,() -> visitorService.findOrCreateVisitor("12@stud.hs"));

        try {
            //Valid external emails
            visitorService.findOrCreateVisitor("test@gmx.de");
            visitorService.findOrCreateVisitor("test_test@gmail.com");
            visitorService.findOrCreateVisitor("t.est@fc-md.umd.edu");
            // Valid internal emails
            visitorService.findOrCreateVisitor("13337@stud.hs-mannheim.de");
            visitorService.findOrCreateVisitor("p.prof@hs-mannheim.de");
        } catch (InvalidEmailException e){
            fail("Valid email caused exception");
        }
    }
}