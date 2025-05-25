package me.oldboy.controllers.webui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.filters.utils.JwtSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String logOut(Authentication authentication,
                         HttpServletRequest request,
                         HttpServletResponse response){
        return "redirect:/webui/bye";
    }

    @GetMapping("/bye")
    public String buy(HttpServletRequest request){
        String email = (String) request.getSession().getAttribute("email");
        authenticationEventListener.setAuthenticationAfterFormLogin(null);
        jwtSaver.setJwtToNull(email);
        request.getSession().invalidate();
        return "/bye.html";
    }
}
