/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.web;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${server_env:production}")
    private String serverEnvironment;

    @Value("${user_credentials:#{null}}")
    private String credentialsEnv;
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**");
    }
	/*@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers("/", "/besuch", "/templates/datenschutz.html").permitAll()
		.antMatchers("/neu", "/neuVer", "/veranstaltungen", "/templates/veranstaltungsliste.html").access("hasAnyRole('USER', 'PROF', 'ADMIN')")
		.mvcMatchers("/tracing/**").hasAnyRole("TRACER", "ADMIN")
		.antMatchers("/suche", "/suchen", "/liste", "/loeschen", "/download", "/h2-console/**", "/r/import", "/printout/rooms", "/printout/rooms/download").access("hasRole('ADMIN')")
		.and().formLogin().loginPage("/login").permitAll()
	    .and().csrf().ignoringAntMatchers("/h2-console/**")
	    .and().headers().frameOptions().sameOrigin();
	}
*/
    @Autowired
    public void globalSecurityConfiguration(AuthenticationManagerBuilder auth) throws Exception {
        if (isDevEnv()) {
            log.warn("Server is running in developer mode with default credentials!");
            // Use plain text passwords for local development
            auth.inMemoryAuthentication().withUser("user").password("user").roles("USER");
            auth.inMemoryAuthentication().withUser("prof").password("prof").roles("PROF");
            auth.inMemoryAuthentication().withUser("admin").password("admin").roles("PROF", "ADMIN");
        } else {
            if (credentialsEnv == null) {
                throw new RuntimeException("No credentials passed as environment variable.");
            }

            val userCredentials = credentialsEnv.split(";");
            for (val credentials : userCredentials) {
                val tokens = credentials.split(",");
                val username = tokens[0];
                val hashedPassword = tokens[1];
                val roles = Arrays.copyOfRange(tokens, 2, tokens.length);

                auth.inMemoryAuthentication()
                        .withUser(username)
                        .password(hashedPassword)
                        .roles(roles);
                log.info("Added user " + username);
            }
        }
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        if (isDevEnv()) {
            // The plain passwords are only used in the dev environment
            // noinspection deprecation
            return NoOpPasswordEncoder.getInstance();
        } else {
            return new BCryptPasswordEncoder();
        }
    }

    private boolean isDevEnv() {
        return serverEnvironment.equals("dev");
    }
}
