package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

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
		httpSecurity
				.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(config -> config
						.requestMatchers(antMatcher("/notices"), antMatcher("/contact"))
						.permitAll()
						.requestMatchers(antMatcher("/myAccount"), antMatcher("/myBalance"), antMatcher("/myLoans"), antMatcher("/myCards"))
						.authenticated()
						.requestMatchers(antMatcher("/admin/**"))
						.hasAuthority("ADMIN")
						.anyRequest()
						.authenticated())
				.httpBasic(Customizer.withDefaults())
				.formLogin(Customizer.withDefaults())
				.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
				.rememberMe((remember) -> remember
						.tokenRepository(tokenRepository())
						.tokenValiditySeconds(86400)
						.key("mySecretKey"));

		return httpSecurity.build();
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