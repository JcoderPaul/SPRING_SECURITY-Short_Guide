package me.oldboy.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.constants.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/* После UsernamePasswordAuthenticationFilter.class */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenGeneratorAndAfterFilter extends OncePerRequestFilter {

	@Autowired
	private final AuthenticationEventListener authenticationEventListener;

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		log.info("-- 3 - Start JwtTokenGenerator or AfterFilter");

		/*
		Поскольку аутентификация проходит через форму, то кроме стандартного запроса на фильтр безопасности прилетят и другие файлы обслуживающие
		форму регистрации, например *.сss и мы потеряем данные аутентификации (сессий то нет, см. цепочку безопасности). По этому мы их сохраняем
		в AuthenticationEventListener после прохождения UsernamePasswordAuthenticationFilter применяем тут если это необходимо и удаляем в финале
		работы этого же фильтра. Чтобы избежать взаимного перекрытия при множественном обращении к нашему приложению, а то получиться, что клиент
		зашел под одной "учеткой", а имеет доступ к данным другой "учетки".
		*/
		Authentication authentication = authenticationEventListener.getAuthenticationAfterFormLogin();

		/* Если же аутентификация уже сохранена в контекст безопасности - достаем данные оттуда */
		if(authentication == null) {
			authentication = SecurityContextHolder.getContext().getAuthentication();
		}
			/* Если аутентификация пройдена - генерируем JWT токен и передаем его в response */
			if (authentication != null) {
				SecretKey key = Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));

				Claims currentClaims = Jwts.claims()
									.subject(authentication.getName())
									.add("authorities", populateAuthorities(authentication.getAuthorities()))
									.build();

				String jwt = Jwts.builder()
								 .claims(currentClaims)
								 .issuedAt(new Date())
								 .expiration(new Date((new Date()).getTime() + 300_000_000))
								 .signWith(key)
								 .compact();

				response.setHeader(SecurityConstants.JWT_HEADER, jwt);

				/* Чистим данные аутентификации сохраненные в AuthenticationEventListener */
				if(isHtmlPage(request)) {
					authenticationEventListener.setAuthenticationAfterFormLogin(null);
					log.info("URL from JwtTokenGenerator or AfterFilter: " + request.getServletPath());
					log.info("After zeroing authFromForm: " + authenticationEventListener.getAuthenticationAfterFormLogin());
				}
			}
		chain.doFilter(request, response);
		log.info("-- 3 - Finish JwtTokenGenerator or AfterFilter");
	}

	private String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
		Set<String> authoritiesSet = new HashSet<>();
		for (GrantedAuthority authority : collection) {
			authoritiesSet.add(authority.getAuthority());
		}
		return String.join(",", authoritiesSet);
	}

	private boolean isHtmlPage(HttpServletRequest request){
		String findPath = request.getServletPath();
		List<String> filteringUrl = List.of("/favicon.ico" ,
				                            "/css/main_menu.css",
				                            "/css/center_of_page.css",
				                            "/css/header_footer.css",
				                            "/css/login_form.css",
				                            "/css/reg_form.css");
		Optional<String> mayBeFindUrl = filteringUrl.stream()
				                                    .filter(url -> url.equals(findPath))
				                                    .findFirst();
		if(mayBeFindUrl.isPresent()){
			return false;
		} else {
			return true;
		}
	}
}