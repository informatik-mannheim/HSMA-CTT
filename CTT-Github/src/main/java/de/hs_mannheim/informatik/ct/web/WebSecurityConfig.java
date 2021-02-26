package de.hs_mannheim.informatik.ct.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {		 
		http.authorizeRequests()
		.antMatchers("/", "/besuch", "/templates/datenschutz.html").permitAll()
		.antMatchers("/neu", "/neuVer", "/veranstaltungen", "/templates/veranstaltungsliste.html").access("hasAnyRole('USER', 'PROF', 'ADMIN')")
		.antMatchers("/suche", "/suchen", "/liste", "/loeschen", "/download", "/h2-console/**","/r/import").access("hasRole('ADMIN')")
		.and().formLogin().loginPage("/login").permitAll()
	    .and().csrf().ignoringAntMatchers("/h2-console/**")
	    .and().headers().frameOptions().sameOrigin();	
	}

	@Autowired
	public void globalSecurityConfiguration(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("user").password("{noop}user").roles("USER");
		auth.inMemoryAuthentication().withUser("prof").password("{noop}prof").roles("PROF");
		auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("PROF","ADMIN");
	}
	
	@Bean
	public SpringSecurityDialect springSecurityDialect() {
	    return new SpringSecurityDialect();
	}

}