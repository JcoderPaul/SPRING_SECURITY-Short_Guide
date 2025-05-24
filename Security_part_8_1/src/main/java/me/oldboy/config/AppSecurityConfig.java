package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.filters.*;
import me.oldboy.filters.utils.RememberMeUserNameExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@ComponentScan("me.oldboy")
public class AppSecurityConfig {

	/* Внедрим "извлекатель" имени из cookie */
	@Autowired
	@Qualifier("rememberMeUserNameExtractor")
	private RememberMeUserNameExtractor rememberMeUserNameExtractor;

	@Autowired
	private PersistentTokenRepository persistentTokenRepository;

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		httpSecurity
				    .addFilterBefore(new MySecondRequestValidationBeforeFilter(rememberMeUserNameExtractor), UsernamePasswordAuthenticationFilter.class)
				    .addFilterBefore(new MyFirstRememberMeBeforeFilter(), UsernamePasswordAuthenticationFilter.class)
				    .addFilterAfter(new MyAuthoritiesLoggingAfterFilter(), UsernamePasswordAuthenticationFilter.class)
				    .addFilterAt(new MyAuthoritiesLoggingAtFilter(), UsernamePasswordAuthenticationFilter.class)
				    .authorizeHttpRequests(urlConfig ->
						urlConfig.requestMatchers(antMatcher("/notices"),
												  antMatcher("/contact"),
												  antMatcher("/css/**"),
												  antMatcher("/webui/login"),
												  antMatcher("/webui/registration")).permitAll()
								  .requestMatchers(antMatcher("/myAccount"),
												   antMatcher("/myBalance"),
												   antMatcher("/myLoans"),
												   antMatcher("/webui/hello")).authenticated()
								  .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
								  .requestMatchers(antMatcher("/myCards")).hasAuthority("READ")
								  .anyRequest().authenticated())
				.rememberMe((remember) -> remember.rememberMeParameter("remember-me")
												  .tokenRepository(persistentTokenRepository)
												  .key("myKey")
												  .alwaysRemember(true))
				.formLogin(login -> login.loginPage("/webui/login")
						                 .defaultSuccessUrl("/webui/hello"));

		return httpSecurity.build();
	}

	@Bean
	UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
		configuration.setAllowedMethods(Collections.singletonList("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	/* Bean который будет помогать в работе с шифрованием/чтением паролей */
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}