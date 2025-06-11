package me.oldboy.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.constants.SecurityConstants;
import me.oldboy.filters.utils.JwtSaver;
import me.oldboy.http_servlet_wrapper.CustomHttpServletRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class SetRequestHeaderFilter extends HttpFilter {

    @Autowired
    private JwtSaver jwtSaver;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.info("-- 1 - Start set request header filter");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        log.info("-- 1 - Set request header filter: " + resp.getHeader(SecurityConstants.JWT_HEADER));

        String userEmail = null;

        /*
        Получаем текущую сессию, созданную в методе *.getJwtAndContinue() класса
        LoginRegController для извлечения значения email c которым ассоциирован
        клиент именно текущей сессии и запроса.
        */
        HttpSession currentSession = req.getSession(false);
        if(currentSession != null) {
            userEmail = (String) currentSession.getAttribute("email");
        }

        /* Достаем токен по значению email-a из "базы хранителя токенов", который имитирует индивидуальный доступ конкретного клиента */
        String setJwtToRequest = jwtSaver.getSavedJwt(userEmail);

        /* Помещаем токен полученный при аутентификации в request header с соответствующим заголовком */
        if (setJwtToRequest != null) {
            CustomHttpServletRequestWrapper customWrappedRequest =
                    new CustomHttpServletRequestWrapper(req); // Подключаем нашу Request обертку
            customWrappedRequest.addHeader(SecurityConstants.JWT_HEADER, setJwtToRequest); // Загружаем (помещаем в коллекцию стандартных header-ов) наш
            chain.doFilter(customWrappedRequest, resp); // Передаем обернутый запрос дальше по цепи фильтров
            log.info("-- 1 - Finish set request header filter: " + setJwtToRequest);
        } else {
            chain.doFilter(req, resp);
            log.info("-- 1 - Finish set request header filter" + " - have no JWT!");
        }

    }
}