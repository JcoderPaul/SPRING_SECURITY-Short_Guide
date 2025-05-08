package me.oldboy.config.security_config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.securiry_details.ClientDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan("me.oldboy")
public class AppSecurityConfig {

	/*
	И снова, без явного внедрения ClientDetailsService зависимости и bean-a AuthenticationManager наше
	приложение будет работать, т.е. комментируем их и все крутиться-вертится, как будто они есть. И это
	так, у нас есть DaoAuthenticationProvider, который в текущей реализации используется по умолчанию.
	Все как в старом фильме, даже если мы не видим суслика - он все равно где-то есть! И самое главное,
	нужно помнить, что любое Spring Web приложение будь то Boot или nonBoot нуждается в контейнере
	сервлетов, который тоже выполняет некую работу будь он внешний или интегрированный.
	*/
	@Autowired
	private ClientDetailsService clientDetailsService;

	@Bean
	public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(clientDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);

		return new ProviderManager(authenticationProvider);
	}

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(config ->
						config.requestMatchers(antMatcher("/notices"),
										       antMatcher("/contact"))
								.permitAll()
								.requestMatchers(antMatcher("/myAccount"),
										antMatcher("/myBalance"),
										antMatcher("/myLoans"),
										antMatcher("/myCards"))
								.authenticated()
								.requestMatchers(antMatcher("/admin/**"))
								.hasAuthority("ADMIN")
								.anyRequest()
								.authenticated())
				.httpBasic(Customizer.withDefaults())
				.formLogin(Customizer.withDefaults());

		return httpSecurity.build();
	}
}