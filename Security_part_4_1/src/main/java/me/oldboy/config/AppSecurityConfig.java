package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan("me.oldboy")
public class AppSecurityConfig {

	/**
	 * Controller endpoint:
	 * /myAccount - Secured
	 * /myBalance - Secured
	 * /myLoans - Secured
	 * /myCards - Secured
	 * /notices - Not Secured
	 * /contact - Not Secured
	 */

	/*
	Что интересно в Spring Boot приложении, так это то, что о многих вещах просто забываешь, т.к. фреймворк на
	основании application.yml проделывает множество настроек сам. И например тут, ситуацию не сильно изменит
	внедрение нашего ClientDetailsService или AuthenticationManager см. комментарий ниже, т.к. на данном этапе
	у нас условно упрощенная конфигурация (мы не делаем каких-то особых настроек) и многие классы и интерфейсы
	подхватываются авто-конфигурацией на основе аннотаций.

	@Autowired
	private ClientDetailsService clientDetailsService;

	@Bean
	public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(clientDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);

		return new ProviderManager(authenticationProvider);
	}

	Можно раскомментировать выше приведенный кусок кода и все будет работать, как и работало, только мы явно
	увидим, как происходит проброс и внедрение компонентов - что полезно.
	*/

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