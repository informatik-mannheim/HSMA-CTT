package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsBesuchRepository;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsRepository;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
public class VeranstaltungsServiceTest extends TestCase {
    @TestConfiguration
    static class VeranstaltungServiceTestContextConfig {
        @Bean
        public VeranstaltungsService service() {
            return new VeranstaltungsService();
        }
    }

    @Autowired
    private VeranstaltungsService veranstaltungsService;

    @MockBean
    private VeranstaltungsRepository veranstaltungRepo;
    @MockBean
    private VisitorRepository besucherRepo;
    @MockBean
    private VeranstaltungsBesuchRepository veranstaltungsBesucherRepo;

    @BeforeAll
    public void setUp() {
        Visitor visitor1 = new Visitor("12345@stud.hs-mannheim.de");
        Visitor visitor2 = new Visitor("13337@stud.hs-mannheim.de");
        int veranstaltungsId = 42;
        String veranstaltungsName = "PR1";
        Date kontaktDate = new Date();
        Date endDate = new Date();

        Collection<VeranstaltungsBesuchDTO> kontakteOf1 = new ArrayList<>();
        kontakteOf1.add(new VeranstaltungsBesuchDTO(
                visitor2.getEmail(),
                veranstaltungsId,
                veranstaltungsName,
                kontaktDate,
                endDate,
                10));

        Collection<VeranstaltungsBesuchDTO> kontakteOf2 = new ArrayList<>();
        kontakteOf2.add(new VeranstaltungsBesuchDTO(
                visitor1.getEmail(),
                veranstaltungsId,
                veranstaltungsName,
                kontaktDate,
                endDate,
                10));

        Mockito.when(besucherRepo.findeKontakteFuer(visitor1.getEmail()))
                .thenReturn(kontakteOf1);
        Mockito.when(besucherRepo.findeKontakteFuer(visitor2.getEmail()))
                .thenReturn(kontakteOf2);
    }

    @Test
    public void testFindeKontakteFuer() {
        String infectedEmail = "13337@stud.hs-mannheim.de";
        Collection<VeranstaltungsBesuchDTO> kontakte = veranstaltungsService.findeKontakteFuer(infectedEmail);

        Mockito.verify(besucherRepo, Mockito.times(1)).findeKontakteFuer(infectedEmail);

        Assertions.assertEquals(kontakte.size(), 1);
        VeranstaltungsBesuchDTO kontakt = kontakte.iterator().next();
        Assertions.assertEquals(kontakt.getBesucherEmail(), "12345@stud.hs-mannheim.de");
    }
}
