package de.hs_mannheim.informatik.ct.end_to_end;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import javax.servlet.http.Cookie;

import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties="allow_full_room_checkIn=false")
@TestPropertySource(properties="warning_for_full_room=true")
public class ContactTracingControllerTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private MockMvc mockMvc;

    private final String TEST_ROOM_NAME = "123";
    private String TEST_ROOM_PIN;
    private final String TEST_USER_EMAIL = "1233920@stud.hs-mannheim.de";

    @BeforeEach
    public void setUp() {
        Room room = new Room(TEST_ROOM_NAME, "A", 10);
        TEST_ROOM_PIN = room.getRoomPin();
        roomService.saveRoom(room);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void loadSearchTracingPage() throws Exception {
        this.mockMvc.perform(
                        get("/tracing/search")

                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(
                        containsString("Nachverfolgung"))
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void showResultIfEmailIsNotCheckedInRoom() throws Exception {
        this.mockMvc.perform(
                        post("/tracing/results")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("email", TEST_USER_EMAIL)
                                .with(csrf())
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(
                        containsString("Eingegebene E-Mail-Adresse konnte im System nicht gefunden werden!"))
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void showTracingResult() throws Exception {
        String email = "0@stud.hs-mannheim.de";
        Room testRoom = roomService.findByName(TEST_ROOM_NAME).get();
        fillRoom(testRoom, 5);
        roomVisitService.checkOutAllVisitors(LocalTime.now());
        this.mockMvc.perform(
                    post("/tracing/results")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("email", email)
                            .with(csrf())
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(
                        containsString("Kontaktverfolgung"))
                )
                .andExpect(content().string(
                        containsString("<td>1@stud.hs-mannheim.de</td>")
                ))
                .andExpect(content().string(
                        containsString("<td>2@stud.hs-mannheim.de</td>")
                ))
                .andExpect(content().string(
                        containsString("<td>3@stud.hs-mannheim.de</td>")
                ))
                .andExpect(content().string(
                        containsString("<td>4@stud.hs-mannheim.de</td>")
                ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void showTracingResultWithoutEmailOutOfScope() throws Exception {
        String emailOutOfScope = "out-of-scope@hs-mannheim.de";
        String emailInScope1 = "in-scope-1@stud.hs-mannheim.de";
        String emailInScope2 = "in-scope-2@stud.hs-mannheim.de";
        Room testRoom = roomService.findByName(TEST_ROOM_NAME).get();

        Optional<Visitor> visitor;

        checkInVisitor(emailOutOfScope, testRoom, dateTimeService.getDate(2022, 11, 15, 12, 0));
        checkInVisitor(emailInScope1, testRoom, dateTimeService.getDate(2022, 11, 16, 12, 0));
        checkInVisitor(emailInScope2, testRoom, dateTimeService.getDate(2022, 11, 16, 12, 0));

        visitor = visitorService.findVisitorByEmail(emailOutOfScope);
        roomVisitService.checkOutVisitor(visitor.get(), dateTimeService.getDate(2022, 11, 15, 18, 0));

        visitor = visitorService.findVisitorByEmail(emailInScope1);
        roomVisitService.checkOutVisitor(visitor.get(), dateTimeService.getDate(2022, 11, 16, 18, 0));

        visitor = visitorService.findVisitorByEmail(emailInScope2);
        roomVisitService.checkOutVisitor(visitor.get(), dateTimeService.getDate(2022, 11, 16, 18, 0));

        this.mockMvc.perform(
                        post("/tracing/results")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("email", emailInScope1)
                                .param("startDate", "2022-11-16")
                                .with(csrf())
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(
                        containsString("Kontaktverfolgung"))
                )
                .andExpect(content().string(
                        doesNotContainString("<td>"+emailOutOfScope+"</td>"))
                )
                .andExpect(content().string(
                        containsString("<td>"+emailInScope2+"</td>")
                ));
    }

    private Matcher<String> doesNotContainString(String s) {
        return CoreMatchers.not(containsString(s));
    }

    private void fillRoom(Room room, int amount) throws InvalidEmailException, InvalidExternalUserdataException {

        for (int i = 0; i < amount; i++) {
            String randomUserEmail = String.format("%d@stud.hs-mannheim.de", i);

            if (randomUserEmail != TEST_USER_EMAIL) {
                checkInVisitor("" + i + "@stud.hs-mannheim.de", room, dateTimeService.getDateNow());
            } else {
                checkInVisitor("0@stud.hs-mannheim.de", room, dateTimeService.getDateNow());
            }
        }
    }

    private void checkInVisitor(String email, Room room, Date date) throws InvalidEmailException, InvalidExternalUserdataException {
        roomVisitService.visitRoom(visitorService.findOrCreateVisitor(email, null, null, null), room, date);
    }
}
