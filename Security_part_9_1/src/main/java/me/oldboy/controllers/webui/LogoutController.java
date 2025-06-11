package me.oldboy.controllers.webui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/webui")
public class LogoutController {

    @Autowired
    private AuthenticationEventListener authenticationEventListener;
    @Autowired
    private JwtSaver jwtSaver;

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
