package me.oldboy.controllers.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class ContactController {
	
	@GetMapping("/contact")
	public String getContactDetails() {
		return "Get details from DB";
	}

}
