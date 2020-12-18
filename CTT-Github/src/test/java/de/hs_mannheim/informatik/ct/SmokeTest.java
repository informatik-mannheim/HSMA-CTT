package de.hs_mannheim.informatik.ct;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.hs_mannheim.informatik.ct.controller.CtController;

@SpringBootTest
public class SmokeTest {
	
	@Autowired
	private CtController controller;

	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

}
