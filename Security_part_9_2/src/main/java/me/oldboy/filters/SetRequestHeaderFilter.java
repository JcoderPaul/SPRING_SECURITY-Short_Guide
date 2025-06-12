package me.oldboy.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.filters.utils.JwtSaver;
import me.oldboy.http_servlet_wrapper.CustomHttpServletRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static me.oldboy.constants.SecurityConstants.EMAIL_COOKIE;
import static me.oldboy.constants.SecurityConstants.JWT_HEADER;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SetRequestHeaderFilter extends HttpFilter {

    @Autowired
    private JwtSaver jwtSaver;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.info("-- 1 - Start set request header filter");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        log.info("-- 1 - Set request header filter: " + resp.getHeader(JWT_HEADER));

        String userEmail = null;

        Cookie[] clientReqCookies = req.getCookies();
        if(clientReqCookies != null){
            Optional<Cookie> emailCookie = Arrays.stream(clientReqCookies)
                    .filter(cookie -> EMAIL_COOKIE.equals(cookie.getName()))
                    .findFirst();
            if (emailCookie.isPresent()) {
                userEmail = emailCookie.get().getValue();
            }
        }

        String setJwtToRequest = jwtSaver.getSavedJwt(userEmail);

        if (setJwtToRequest != null) {
            CustomHttpServletRequestWrapper customWrappedRequest =
                    new CustomHttpServletRequestWrapper(req); // Подключаем нашу Request обертку
            customWrappedRequest.addHeader(JWT_HEADER, setJwtToRequest); // Загружаем (помещаем в коллекцию стандартных header-ов) наш
            chain.doFilter(customWrappedRequest, resp); // Передаем обернутый запрос дальше по цепи фильтров
            log.info("-- 1 - Finish set request header filter: " + setJwtToRequest);
        } else {
            chain.doFilter(req, resp);
            log.info("-- 1 - Finish set request header filter" + " - have no JWT!");
        }
    }
}