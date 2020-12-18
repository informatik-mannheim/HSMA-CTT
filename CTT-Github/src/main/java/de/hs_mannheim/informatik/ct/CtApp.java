package de.hs_mannheim.informatik.ct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CtApp {

	public static void main(String[] args) {
		SpringApplication.run(CtApp.class, args);
	}

}