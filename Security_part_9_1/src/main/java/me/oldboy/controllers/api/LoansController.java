package me.oldboy.controllers.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoansController {
	@GetMapping("/myLoans")
	public String getLoanDetails() {
		return "Here are the loan details from the DB";
	}

}
