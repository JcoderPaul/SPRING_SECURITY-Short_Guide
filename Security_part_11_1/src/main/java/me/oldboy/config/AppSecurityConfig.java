package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.logout_handler.CustomLogoutHandler;
import me.oldboy.config.security_ext.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@ComponentScan("me.oldboy")
public class AppSecurityConfig {

	private final String KEY = "myKey";

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private CustomLogoutHandler customLogoutHandler;
	@Autowired
	private PersistentTokenRepository persistentTokenRepository;
	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		httpSecurity
				.csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF
				.authorizeHttpRequests(urlConfig -> urlConfig
						.requestMatchers("/webui/login",
								"/webui/registration",
								"/webui/bye",
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
						.requestMatchers(antMatcher("/webui/cards"),
								antMatcher("/myCards")).hasAuthority("READ")
						.anyRequest().authenticated())
				.logout(logout -> logout.logoutUrl("/webui/logout")
						.logoutSuccessUrl("/webui/bye")
						.addLogoutHandler(customLogoutHandler)
						.deleteCookies("JSESSIONID")
						.invalidateHttpSession(true))
				.formLogin(login -> login.loginPage("/webui/login")
						.defaultSuccessUrl("/webui/main"))
				.oauth2Login(oauthConfig -> oauthConfig.loginPage("/webui/login")
						.defaultSuccessUrl("/webui/main")
						.userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
				.rememberMe((remember) -> remember.tokenRepository(persistentTokenRepository)
						.tokenValiditySeconds(86400)
						.key(KEY));

		return httpSecurity.build();
	}

	/* Повторим пройденное - нам нужно адаптировать credentials из Google (GitHub) и внутренние authentication настройки приложения */
	private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
		return userRequest -> {
			/* Извлекаем email из userRequest */
			String email = userRequest.getIdToken().getClaim("email");
			/*
				Как вариант, тут можно создать user-a, если он не зарегистрирован в БД - userService.create.
				Но на данном этапе мы извлекаем существующего пользователя из таблицы users нашей БД с его
				Authorities (не из Google сервиса), предполагая что он есть.
			*/
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
			/* Мы должны вернуть DefaultOidcUser - создаем его из полученного UserDetails и idToken-a */
			DefaultOidcUser oidcUser = new DefaultOidcUser(userDetails.getAuthorities(), userRequest.getIdToken());

			Set<Method> userDetailsMethods = Set.of(UserDetails.class.getMethods());

			/* Можно подставить AbstractAuthenticationToken вместо AppSecurityConfig */
			ClassLoader appSecConfigClassLoader = AbstractAuthenticationToken.class.getClassLoader();
			/*
				Теперь самое сложное - вернуть прокси, который в случае обращения к UserDetails
				вернет его, а в случае обращения к OidcUser вернет уже его реализацию. И тут очень
				к стати нам то, что и тот и другой интерфейсы и мы можем использовать динамический
				прокси.
			*/
			return (OidcUser) Proxy.newProxyInstance(appSecConfigClassLoader,
					new Class[]{UserDetails.class, OidcUser.class},
					(proxy, method, args) -> userDetailsMethods.contains(method)
							? method.invoke(userDetails, args)
							: method.invoke(oidcUser, args));
		};
	}
}