package me.oldboy.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@ComponentScan("me.oldboy")
public class SecurityConfig {

	@Value("${spring.security.user.name}")
	private String userName;
	@Value("${spring.security.user.password}")
	private String password;

	/**
	 * Controller endpoint:
	 * /myAccount - Secured
	 * /myBalance - Secured
	 * /myLoans - Secured
	 * /myCards - Secured
	 * /notices - Not Secured
	 * /contact - Not Secured
	 */

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
								.authenticated())
				.httpBasic(Customizer.withDefaults())
				.formLogin(Customizer.withDefaults());

		return httpSecurity.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User
				.withUsername(userName)
				.password(password)
				.build();

		return new InMemoryUserDetailsManager(userDetails);
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
