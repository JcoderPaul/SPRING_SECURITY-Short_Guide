package me.oldboy.controllers.webui;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/webui")
public class WebAccountController {

    @GetMapping("/myAccount")
    public ResponseEntity<String> showAccountToWebPage(){
        return ResponseEntity.ok().body("My account data from DB");
    }
}

