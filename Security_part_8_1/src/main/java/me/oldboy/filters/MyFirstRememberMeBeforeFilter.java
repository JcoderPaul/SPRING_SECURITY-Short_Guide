package me.oldboy.filters;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/*
Мы хотели извлечь имя клиента из БД по имеющемуся remember-me токену, как оказалось, на определенном этапе
работы цепочки фильтров, стандартными средствами, это не просто. По этому тут мы просто зафиксируем наличие
(или отсутствие) remember-me cookie в запросе и будем извлекать имя по возможности, когда оно уже попадет в
SecurityContext.

Данный фильтр будет внедрен перед UsernamePasswordAuthenticationFilter - используем метод *.addFilterBefore()
цепочки безопасности, см. AppSecurityConfig.java
*/
@Slf4j
@RequiredArgsConstructor
public class MyFirstRememberMeBeforeFilter implements Filter {

	private static final String REMEMBER_ME = "remember-me";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;

		Cookie[] cookies = req.getCookies();
		if(cookies == null || Arrays.stream(cookies).filter(cookie -> REMEMBER_ME.equals(cookie.getName())).findFirst().isEmpty()){
			log.info(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
					 " *** User try to authentication! Have no Remember-Me cookies! *** ");
		} else {
			log.info(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
					 " *** User is already authenticated by Remember-Me Token! *** ");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null) {
				log.info(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
						 " *** User " + authentication.getName() +
						 " is already authenticated by Remember-Me Token! *** ");
			}
		}
		chain.doFilter(request, response);
	}
}
