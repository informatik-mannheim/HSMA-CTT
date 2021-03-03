package de.hs_mannheim.informatik.ct.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {		 
		http.authorizeRequests()
		.antMatchers("/", "/besuch", "/templates/datenschutz.html").permitAll()
		.antMatchers("/neu", "/neuVer", "/veranstaltungen", "/templates/veranstaltungsliste.html").access("hasAnyRole('USER', 'PROF', 'ADMIN')")
		.antMatchers("/suche", "/suchen", "/liste", "/loeschen", "/download", "/h2-console/**","/r/import","/printout/rooms").access("hasRole('ADMIN')")
		.and().formLogin().loginPage("/login").permitAll()
	    .and().csrf().ignoringAntMatchers("/h2-console/**")
	    .and().headers().frameOptions().sameOrigin();	
	}

	@Autowired
	public void globalSecurityConfiguration(AuthenticationManagerBuilder auth) throws Exception {
//		System.out.println("hash -> " + passwordEncoder().encode("passwordYouWannaEncode"));

		auth.inMemoryAuthentication().withUser("user").password("$2a$10$WUJevKFYLHfIheVZ3yv7J.7uIHeoPV8fAb9wFqdW50kFD8O4EWJ4u").roles("USER");
		auth.inMemoryAuthentication().withUser("prof").password("$2a$10$WUJevKFYLHfIheVZ3yv7J.7uIHeoPV8fAb9wFqdW50kFD8O4EWJ4u").roles("PROF");
		auth.inMemoryAuthentication().withUser("admin").password("$2a$10$nW.GjVHey9UA47Xv8V8yHe5WQ67rNLsI3bjdi8gIM/28MufxGc53a").roles("PROF","ADMIN");
				
		auth.inMemoryAuthentication().withUser("k.albert@hs-mannheim.de").password("$2a$10$GTSP6jJBQD58zd3RQz62zeHIGfCEjMp7Lcjg77jkXh.zbvT9qBKCa").roles("PROF","ADMIN");
	}
	
	@Bean
	public SpringSecurityDialect springSecurityDialect() {
	    return new SpringSecurityDialect();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}

}