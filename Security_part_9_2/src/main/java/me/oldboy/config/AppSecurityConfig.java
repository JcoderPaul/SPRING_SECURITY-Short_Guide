package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.config.logout_handler.CustomLogoutHandler;
import me.oldboy.filters.JwtTokenGeneratorAndAfterFilter;
import me.oldboy.filters.JwtTokenValidatorAndBeforeFilter;
import me.oldboy.filters.SetRequestHeaderFilter;
import me.oldboy.filters.utils.JwtSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
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
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@ComponentScan("me.oldboy")
public class AppSecurityConfig {

	private final String KEY = "myKey";

	@Autowired
	private DataSource dataSource;
	@Autowired
	private AuthenticationEventListener authenticationEventListener;
	@Autowired
	private CustomLogoutHandler customLogoutHandler;
	@Autowired
	private JwtSaver jwtSaver;

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		httpSecurity
					.csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF
					.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS)) // Определяем бессессионный режим
				    .addFilterBefore(new JwtTokenValidatorAndBeforeFilter(authenticationEventListener), UsernamePasswordAuthenticationFilter.class) // Размещаем наш валидатор токена перед фильтром аутентификации
				    .addFilterAfter(new JwtTokenGeneratorAndAfterFilter(authenticationEventListener), UsernamePasswordAuthenticationFilter.class) // Размещаем наш генератор токена после фильтром аутентификации
				    .addFilterAfter(new SetRequestHeaderFilter(jwtSaver), LogoutFilter.class) // Размещаем установщик request header-a после Logout фильтра, т.е. строго перед нашим же фильтром валидатором токена
					.authorizeHttpRequests(urlConfig ->
						urlConfig.requestMatchers("/webui/login",
								                           "/webui/registration",
								                           "/webui/bye",
								                           "/webui/jwt_token",
										                   "/notices",
														   "/css/*.css").permitAll()
								  .requestMatchers(antMatcher("/myAccount"),
												   antMatcher("/myBalance"),
												   antMatcher("/myLoans"),
										  		   antMatcher("/webui/account"),
										  		   antMatcher("/webui/balance"),
												   antMatcher("/webui/loans"),
										           antMatcher("/webui/contacts"),
										           antMatcher("/webui/main")).authenticated()
								  .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
								  .requestMatchers( antMatcher("/webui/cards"), antMatcher("/myCards")).hasAuthority("READ")
								  .anyRequest().authenticated())
				    .formLogin(login -> login.loginPage("/webui/login")
					    	                 .defaultSuccessUrl("/webui/jwt_token"))
      				.logout(logout -> logout.logoutUrl("/webui/logout")
											.logoutSuccessUrl("/webui/bye")
											.addLogoutHandler(customLogoutHandler)
											.deleteCookies("JSESSIONID")
											.invalidateHttpSession(true));

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