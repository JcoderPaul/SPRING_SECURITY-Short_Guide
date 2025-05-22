package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.securiry_details.ClientDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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

	@Autowired
	private ClientDetailsService clientDetailService;

	/* Bean который будет помогать в работе с шифрованием/чтением паролей */
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable)
				.authenticationProvider(authenticationProvider())
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

	/*
		Теоретически мы могли бы и не создавать данный bean и приложение "само разрешило бы конфликт",
		но, в логах мы бы наблюдали предупреждение:

		WARN 12948 --- [main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured
		with an AuthenticationProvider bean. UserDetailsService beans will not be used for username/password login.
		Consider removing the AuthenticationProvider bean. Alternatively, consider using the UserDetailsService in
		a manually instantiated DaoAuthenticationProvider.

  		хотя приложение при этом отрабатывало свой функционал при ручном тестировании с PostMan.
	*/
	@Bean
	public AuthenticationProvider authenticationProvider() throws Exception {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(clientDetailService);
		authProvider.setPasswordEncoder(getPasswordEncoder());
		return authProvider;
	}
}