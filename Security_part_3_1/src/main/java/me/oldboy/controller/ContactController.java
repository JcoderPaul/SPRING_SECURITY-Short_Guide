package me.oldboy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController {
	
	@GetMapping("/contact")
	public String getContactDetails() {
		return "Get details from DB";
	}

}
