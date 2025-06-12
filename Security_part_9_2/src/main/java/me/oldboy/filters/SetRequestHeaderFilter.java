package me.oldboy.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.filters.utils.JwtSaver;
import me.oldboy.http_servlet_wrapper.CustomHttpServletRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static me.oldboy.constants.SecurityConstants.JWT_HEADER;
import static me.oldboy.constants.SecurityConstants.EMAIL_COOKIE;

@Slf4j
@RequiredArgsConstructor
public class SetRequestHeaderFilter extends HttpFilter {

    @Autowired
    private final JwtSaver jwtSaver;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

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

    @Override
    public void destroy() {

    }
}