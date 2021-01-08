package de.hs_mannheim.informatik.ct.web;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpRequestTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /*
    MockMVC hier besser?
     */
    @Test
    public void landingPageTextCheck() throws Exception{
        assertThat(this.restTemplate.getForObject("http://localhost:"+port+"/",String.class)).contains("Willkommen beim CTT, dem Corona Tracking Tool der Hochschule Mannheim.");
    }

    @Test
    public void nonexistentPageGetsCalled() throws Exception{
        assertThat(this.restTemplate.getForObject("http://localhost:"+port+"/no",String.class)).contains("Fehler-Code: 404");

    }

    @Test
    public void landingPageDoesNotContainErrors() throws Exception{
        assertThat(this.restTemplate.getForObject("http://localhost:"+port+"/",String.class)).doesNotContain("Fehler-Code");
    }

}


