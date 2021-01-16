package de.hs_mannheim.informatik.ct.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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
    public void landingPageDefaultTest() throws Exception{
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Willkommen beim CTT, dem Corona Tracking Tool der Hochschule Mannheim.")));
    }

    @Test
    public void nonexistentPageGetsCalled() throws Exception{
        this.mockMvc.perform(get("/no")).andDo(print()).andExpect(status().isNotFound());

    }


}
