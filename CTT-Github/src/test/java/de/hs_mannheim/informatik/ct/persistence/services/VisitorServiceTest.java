package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VisitorServiceTest {
    @TestConfiguration
    static class VisitorServiceTestConfig {
        @Bean
        public VisitorService visitorService() {
            return new VisitorService();
        }
    }

    @MockBean
    private VisitorRepository visitorRepository;

    @Autowired
    private VisitorService visitorService;

    private AutoCloseable mocks;

    @BeforeEach
    public void openMocks() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mocks.close();
    }

    /**
     * There was a bug that caused the findOrCreateVisitor method to save the visitor even if it was found. This causes
     * a constraint violation, Therefore ensure that save is ONLY called if the visitor wasn't found.
     */
    @Test
    void finOrCreateNoDuplicateSave() {
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
