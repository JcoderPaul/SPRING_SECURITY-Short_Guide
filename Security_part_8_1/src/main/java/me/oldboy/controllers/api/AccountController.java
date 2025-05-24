package me.oldboy.controllers.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AccountController {
	
	@GetMapping("/myAccount")
	public String getAccountDetails(String input) {
		return "Here are the account details from the DB";
	}

}
