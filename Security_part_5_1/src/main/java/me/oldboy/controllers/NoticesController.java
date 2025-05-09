package me.oldboy.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class NoticesController {
	
	@GetMapping("/notices")
	public String getNotices(String input) {
		return "Here are the notices details from the DB";
	}

}
