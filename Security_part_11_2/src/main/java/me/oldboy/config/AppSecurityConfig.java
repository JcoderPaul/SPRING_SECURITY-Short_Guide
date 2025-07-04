package me.oldboy.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@ComponentScan("me.oldboy")
@AllArgsConstructor
@NoArgsConstructor
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
	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity,  ClientRegistrationRepository clientRegistrationRepository) {
		httpSecurity
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(urlConfig -> urlConfig
						.requestMatchers("/webui/login",
								"/webui/registration",
								"/api/regClient",
								"/webui/bye",
								"/api/notices",
								"/css/*.css").permitAll()
						.requestMatchers("/api/myAccount",
								"/api/myBalance",
								"/api/myLoans",
								"/webui/account",
								"/webui/balance",
								"/webui/loans",
								"/webui/contacts",
								"/webui/main").authenticated()
						.requestMatchers(antMatcher("/api/admin/**")).hasRole("ADMIN")
						.requestMatchers("/webui/cards", "/api/myCards").hasAuthority("READ")
						.anyRequest().authenticated())
				.logout(logout -> logout.logoutUrl("/webui/logout")
						.logoutSuccessUrl("/webui/bye")
						.deleteCookies("JSESSIONID")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.addLogoutHandler(customLogoutHandler)
						.logoutSuccessHandler(oidcLogoutSuccessHandler()) // Он имеет приоритет над обычным обработчиком, что выше
						.permitAll())
				.formLogin(login -> login.loginPage("/webui/login").permitAll()
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

	private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
		return userRequest -> {
			String email = userRequest.getIdToken().getClaim("email");
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);

			DefaultOidcUser oidcUser = new DefaultOidcUser(userDetails.getAuthorities(), userRequest.getIdToken());
			Set<Method> userDetailsMethods = Set.of(UserDetails.class.getMethods());

            ClassLoader appSecConfigClassLoader = AbstractAuthenticationToken.class.getClassLoader();

			return (OidcUser) Proxy.newProxyInstance(appSecConfigClassLoader,
					new Class[]{UserDetails.class, OidcUser.class},
					(proxy, method, args) -> userDetailsMethods.contains(method)
							? method.invoke(userDetails, args)
							: method.invoke(oidcUser, args));
		};
	}

	private LogoutSuccessHandler oidcLogoutSuccessHandler() {
		OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
				new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
		/*
			Указываем URI, на который Keycloak перенаправит пользователя после выхода (logout),
			данный URI необходимо добавить в раздел "Valid Post Logout Redirect URIs" в настройках
			Keycloak
		*/
		oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:8080/webui/bye");

		return oidcLogoutSuccessHandler;
	}
}