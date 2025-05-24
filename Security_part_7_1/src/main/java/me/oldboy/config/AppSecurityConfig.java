package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.Collections;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan("me.oldboy")
public class AppSecurityConfig {

	@Autowired
	private DataSource dataSource;

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		httpSecurity.authorizeHttpRequests(urlConfig ->	urlConfig.
						requestMatchers(antMatcher("/notices"),
								antMatcher("/contact"),
								antMatcher("/css/**"),
								antMatcher("/webui/login"),
								antMatcher("/webui/registration"))
						.permitAll()
						.requestMatchers(antMatcher("/myAccount"),
								antMatcher("/myBalance"),
								antMatcher("/myLoans"),
								antMatcher("/myCards"),
								antMatcher("/webui/hello"))
						.authenticated()
						.requestMatchers(antMatcher("/admin/**"))
						.hasAuthority("ADMIN")
						.anyRequest().authenticated())
						.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
						.rememberMe((remember) -> remember.rememberMeParameter("remember-me")
								.tokenRepository(tokenRepository())
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

	@Bean
	public PersistentTokenRepository tokenRepository() {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		return tokenRepository;
	}

	/* Bean который будет помогать в работе с шифрованием/чтением паролей */
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}