package me.oldboy.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.filters.utils.RememberMeUserNameExtractor;

import java.io.IOException;
import java.util.Arrays;


/*
Задача не решенная в MyFirstRememberMeBeforeFilter осталась актуальной, мы хотим извлечь userName именно из remember-me
token-a, если еще более точно - проверить, есть ли в полученном request-e cookie с именем "remember-me" и если есть,
используя его получить требуемое. Например, так - извлечь содержимое cookie, распарсить, обратиться к БД и по series
получить требуемый токен, а уже из токена извлечь userName. Для этой задачи мы написали класс RememberMeUserNameExtractor,
который через конструктор внедряем сюда, см. ниже.
*/
@Slf4j
@RequiredArgsConstructor
public class MySecondRequestValidationBeforeFilter implements Filter {

	private static final String REMEMBER_ME = "remember-me";

	private final RememberMeUserNameExtractor rememberMeUserNameExtractor;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;

		Cookie[] cookies = req.getCookies();
		if(cookies == null || Arrays.stream(cookies)
				                    .filter(cookie -> REMEMBER_ME.equals(cookie.getName()))
				                    .findFirst()
				                    .isEmpty()) {
			log.info(" \n *** 2 - Log MySecondRequestValidationBeforeFilter method *** \n" +
					 " *** User try to authentication! Have no Remember-Me cookies! *** ");
		} else {
			rememberMeUserNameExtractor.getUserNameFromToken(req)
					                   .ifPresent(userName -> log.info(" \n *** 2 - Log MySecondRequestValidationBeforeFilter method *** \n" +
					                                                   " *** User " + userName + " is already authenticated by Remember-Me Token! *** "));
		}

		chain.doFilter(request, response);
	}
}
