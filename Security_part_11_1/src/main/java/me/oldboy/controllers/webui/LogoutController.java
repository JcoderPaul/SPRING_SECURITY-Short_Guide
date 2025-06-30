package me.oldboy.controllers.webui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/webui")
@RequiredArgsConstructor
public class LogoutController {

    @GetMapping("/exit")
    public String exitApp(){
        return "client_forms/logout.html";
    }

    @PostMapping("/logout")
    public String logOut(){
        return "redirect:/webui/bye";
    }

    @GetMapping("/bye")
    public String buy(){
        return "/bye.html";
    }
}