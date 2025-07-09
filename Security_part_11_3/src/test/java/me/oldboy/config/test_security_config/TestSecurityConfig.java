package me.oldboy.config.test_security_config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.security_config.FilterChainConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@ComponentScan("me.oldboy.config.test_main")
public class TestSecurityConfig {

	@Autowired
	private JwtAuthenticationConverter jwtAuthenticationConverter;

	@Bean
	public JwtDecoder testJwtDecoder() {
		// Укажите JWK Set URI, который будет указывать на WireMock
		return NimbusJwtDecoder.withJwkSetUri("http://localhost:8089/auth/realms/test-realm/protocol/openid-connect/certs").build();
	}

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		return FilterChainConfig.getSecurityFilterChain(httpSecurity, testJwtDecoder(), jwtAuthenticationConverter);
	}
}