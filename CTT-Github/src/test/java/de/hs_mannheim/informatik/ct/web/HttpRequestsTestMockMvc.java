package de.hs_mannheim.informatik.ct.web;

import de.hs_mannheim.informatik.ct.controller.CtController;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.persistence.repositories.VeranstaltungsRepository;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import javax.transaction.Transactional;

import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HttpRequestsTestMockMvc {

    @Autowired
    private MockMvc mockMvc;






    @Test
    @Transactional
    public void landingPageDefaultTest() throws Exception{
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Willkommen beim CTT, dem Corona Tracking Tool der Hochschule Mannheim.")));

    }

    @Test
    public void nonexistentPageGetsCalled() throws Exception{
        this.mockMvc.perform(get("/no")).andDo(print()).andExpect(status().isNotFound());

    }

    @Test
    @Transactional
    public void getCorrectViewNames() throws Exception {
        this.mockMvc.perform(get("/")).andExpect(MockMvcResultMatchers.view().name("index"));
        this.mockMvc.perform(get("/r/import")).andExpect(MockMvcResultMatchers.view().name("rooms/roomImport"));
    }



    //TODO: Wie kann ich hier Räume vorher anlegen, damit die Tests auch dann richtig auswerten? TestEntityManager? Geht das überhaupt auf dem Weblayer?

    @Test
    public void getCorrectRoomViews() throws Exception {
        this.mockMvc.perform(get("/r/A008")).andExpect(MockMvcResultMatchers.view().name("rooms/checkIn"));
        this.mockMvc.perform(get("/r/roomFull/A008")).andExpect(MockMvcResultMatchers.view().name("rooms/full"));

    }


}
