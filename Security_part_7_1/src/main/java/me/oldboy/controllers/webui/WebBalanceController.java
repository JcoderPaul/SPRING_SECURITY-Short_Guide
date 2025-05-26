package me.oldboy.controllers.webui;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/webui")
public class WebBalanceController {

    @GetMapping("/myBalance")
    public ResponseEntity<String> showMyBalanceToWebPage(){
        return ResponseEntity.ok().body("My balance from base");
    }
}

