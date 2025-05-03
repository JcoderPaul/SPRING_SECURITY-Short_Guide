package me.oldboy.config;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.likebase.LikeBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Slf4j
@Configuration
public class ProjectSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private LikeBase likeBase;
	/**
	 * /myAccount - Secured
	 * /myBalance - Secured
	 * /myLoans - Secured
	 * /myCards - Secured
	 * /clientList - Secured only for ADMIN
	 * /notices - Not Secured
	 * /contact - Not Secured
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/notices", "/contact")
				.permitAll()
				.antMatchers("/myAccount", "/myBalance", "/myLoans", "/myCards")
				.authenticated()
				.antMatchers("/clientList")
				.hasAuthority("ADMIN")
				.and()
				.formLogin()
				.and()
				.httpBasic();
	}

	/* Конфигурируем менеджер аутентификации, в данном случае мы будем хранить данные в памяти */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		InMemoryUserDetailsManager userDetailsService = new	InMemoryUserDetailsManager();

		/*
		В самой первой версии программы мы брали пароль и логин пользователя из файла настроек application.yml,
		который по умолчанию подхватывается Spring Boot приложением и в нем должны находиться соответствующие,
		заранее обговоренные в документации пары (key:value):

			spring.security.user.name:
			spring.security.user.password:

		В данном случае мы немного изменили подход и использовали другой способ определения паролей и логинов,
		а так же способ их размещать в менеджере аутентификации, см. ниже.
		*/
		likeBase.loadUserList();

		UserDetails userOne = likeBase.getUserList().get(0);
	 	UserDetails userTwo = likeBase.getUserList().get(1);

		userDetailsService.createUser(userOne);
		userDetailsService.createUser(userTwo);

		/* Можно сделать так */
		auth.userDetailsService(userDetailsService);

		/*
		Можно хардкодом, и тогда в памяти будет размещено 3-и пользователя, два из
		файла application.properties и еще один заданный описанным ниже способом.
		*/
		auth.inMemoryAuthentication().withUser("gosha").password("kuciy").authorities("READ");
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
