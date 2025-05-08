package me.oldboy.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class NoticesController {

	/*
	Если применить данный метод в следующей конфигурации для non Boot Spring приложения:

	@GetMapping("/notices")
	public String getNotices(String inputData) {
		return "Here are the notices details from the DB";
	}

	То будет брошено исключение:

	{"exceptionMsg":"Name for argument of type [java.lang.String] not specified, and parameter name information
	not available via reflection. Ensure that the compiler uses the '-parameters' flag."}

	Однако в Spring Boot приложении все проходит красиво.
	*/
	@GetMapping("/notices")
	public String getNotices() {
		return "Here are the notices details from the DB";
	}

}
