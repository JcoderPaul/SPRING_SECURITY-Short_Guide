package me.oldboy.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	/*
	У нас есть эндпоинты:

	/myAccount - Secured
	/myBalance - Secured
	/myLoans - Secured
	/myCards - Secured
	/userList - Secured
	/notices - Not Secured
	/contact - Not Secured

	Нам никто не запрещает делать, например так:

	http.authorizeRequests().antMatchers("/myAccount").authenticated()
	                        .antMatchers("/myBalance").authenticated()
				            .antMatchers("/myLoans").authenticated()
				            .antMatchers("/myCards").authenticated()
				            .antMatchers("/notices").permitAll()
				            .antMatchers("/contact").permitAll()
				            .and().formLogin().and().httpBasic();

	Вариант, когда в один метод сравнения мы через запятую размещаем
	нужные эндпоинты, со схожим уровнем доступа, тоже приемлем:
	*/

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/notices", "/contact").permitAll()
				.antMatchers("/myAccount", "/myBalance", "/myLoans", "/myCards").authenticated()
				.antMatchers("/userList").hasAnyRole("HR")
				.anyRequest().authenticated()
				.and()
				.formLogin()
				.and()
				.httpBasic();
	}

	/*
	Ключевым моментом тут является Менеджер аутентификации и в ранних версиях приложения,
	когда мы хранили/и доставали пароли и логины из памяти мы указывали, что именно оттуда
	брать данные:

	InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();

	Теперь источником паролей/логинов является БД - указываем это:
	*/

	@Bean
	public UserDetailsService userDetailsService(DataSource dataSource) {
		return new JdbcUserDetailsManager(dataSource);
	}

	/*
	Применение устаревшего или Deprecated класса/метода может привести к весьма неожиданным последствиям,
	например, создание кодировщика в старом варианте кода, могло быть таким:

		@Bean
		public PasswordEncoder passwordEncoder() {
			return NoOpPasswordEncoder.getInstance();
		}

	Если проигнорировать предупреждение о том, что класс NoOpPasswordEncoder устарел, и запустить приложение,
	то оно будет работать, но весьма пикантно. Поскольку в текущей реализации мы не шифруем пароль, и явно
	указываем это префиксом {noop}, то при обращении к защищенной странице нам вместо заданного пароля придется
	вводить полную строку с префиксом, словно он является частью пароля.
	*/

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}