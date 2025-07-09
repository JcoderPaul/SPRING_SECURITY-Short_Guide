package me.oldboy.config.security_config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true) // позволяет фиксировать происходящее в режиме отладки
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@ComponentScan("me.oldboy.config.main_config")
@AllArgsConstructor
@NoArgsConstructor
public class AppSecurityConfig {

	@Autowired
	private JwtDecoder jwtDecoder;
	@Autowired
	private JwtAuthenticationConverter jwtAuthenticationConverter;

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		return FilterChainConfig.getSecurityFilterChain(httpSecurity, jwtDecoder, jwtAuthenticationConverter);
	}
}