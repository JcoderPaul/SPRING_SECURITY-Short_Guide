package me.oldboy.config.test_security_config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@ComponentScan("me.oldboy.config.test_main_config")
public class TestSecurityConfig {

	@Autowired
	private PersistentTokenRepository persistentTokenRepository;

	@Bean
	@SneakyThrows
	@Order(1)
	public SecurityFilterChain webFilterChain(HttpSecurity httpSecurity) {
		httpSecurity
				.securityMatcher("/webui/**", "/")
				.authorizeHttpRequests(urlConfig -> urlConfig
						.requestMatchers(antMatcher("/api/notices"),
								antMatcher("/api/contact"),
								antMatcher("/static/**"),
								antMatcher("/webui/login"),
								antMatcher("/webui/registration"))
						.permitAll()
						.requestMatchers(antMatcher("/webui/myAccount"),
								antMatcher("/webui/myBalance"),
								antMatcher("/webui/hello"))
						.authenticated()
						.anyRequest().authenticated())
				.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
				.rememberMe((remember) -> remember.rememberMeParameter("remember-me")
						.tokenRepository(persistentTokenRepository)
						.alwaysRemember(true))
				.formLogin(login -> login.loginPage("/webui/login")
						.defaultSuccessUrl("/webui/hello"));

		return httpSecurity.build();
	}

	@Bean
	@SneakyThrows
	@Order(5)
	public SecurityFilterChain restFilterChain(HttpSecurity httpSecurity) {
		httpSecurity
				.csrf(AbstractHttpConfigurer::disable)
				.securityMatcher("/api/**")
				.authorizeHttpRequests(urlConfig -> urlConfig
						.requestMatchers(antMatcher("/api/notices"),
								antMatcher("/api/contact"))
						.permitAll()
						.requestMatchers(antMatcher("/api/myAccount"),
								antMatcher("/api/myBalance"),
								antMatcher("/api/myLoans"))
						.authenticated()
						.requestMatchers(antMatcher("/api/admin/**"))
						.hasAnyRole("ADMIN")
						.requestMatchers(antMatcher("/api/myCards"))
						.hasAuthority("READ")
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults());

		return httpSecurity.build();
	}
}