package me.oldboy.config.test_security_config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.security_config.FilterChainConfig;
import me.oldboy.filters.JwtTokenGeneratorAndAfterFilter;
import me.oldboy.filters.JwtTokenValidatorAndBeforeFilter;
import me.oldboy.filters.UserPassValidatorAndAfterLogoutFilter;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true) // позволяет фиксировать происходящее в режиме отладки
@EnableMethodSecurity
@ComponentScan("me.oldboy.config.test_main")
public class TestSecurityConfig {

	private final String KEY = "myKey";

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private ClientService clientService;

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		return FilterChainConfig.getSecurityFilterChain(httpSecurity, userDetailsService, clientService);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	/* Bean который будет помогать в работе с шифрованием/чтением паролей */
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}