package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.services.ClientService;
import me.oldboy.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
public class LoansController {

	@Autowired
	private ClientService clientService;
	@Autowired
	private LoanService loanService;
	
	@GetMapping("/myLoans")
	public ResponseEntity<List<LoanReadDto>> getLoanDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsDetector userDetailsDetector = new UserDetailsDetector();
		ResponseEntity<List<LoanReadDto>> loansList = ResponseEntity.noContent().build();

		if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)){
			long clientId = userDetailsDetector.getClientId();

			List<LoanReadDto> mayBeLoans = loanService.readAllLoansByUserId(clientId);
			if(mayBeLoans.size() != 0){
				loansList = ResponseEntity.ok().body(mayBeLoans);
			}
		}
		return loansList;
	}
}