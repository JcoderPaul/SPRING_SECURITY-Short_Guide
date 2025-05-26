package me.oldboy.controllers.webui;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.filters.utils.JwtSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Optional;

import static me.oldboy.constants.SecurityConstants.EMAIL_COOKIE;

@Slf4j
@Controller
@RequestMapping("/webui")
@RequiredArgsConstructor
public class LogoutController {

    @Autowired
    private final AuthenticationEventListener authenticationEventListener;
    @Autowired
    private final JwtSaver jwtSaver;

    @PostMapping("/logout")
    public String logOut(){
        return "redirect:/webui/bye";
    }

    @GetMapping("/bye")
    public String buy(HttpServletRequest request, HttpServletResponse response){
        authenticationEventListener.setAuthenticationAfterFormLogin(null);

        Cookie[] clientReqCookies = request.getCookies();
        if(clientReqCookies != null){
            Optional<Cookie> emailCookie = Arrays.stream(clientReqCookies)
                                                 .filter(cookie -> EMAIL_COOKIE.equals(cookie.getName()))
                                                 .findFirst();
            if (emailCookie.isPresent()) {
                String email = emailCookie.get().getValue();
                jwtSaver.setJwtToNull(email);
            }
        }

        Cookie removeCookie = new Cookie(EMAIL_COOKIE, null);
        removeCookie.setPath("/");
        removeCookie.setMaxAge(0);
        response.addCookie(removeCookie);

        return "/bye.html";
    }
}