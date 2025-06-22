package me.oldboy.controllers.api;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.services.AccountService;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class AccountController {

	@Autowired
	private AccountService accountService;
	@Autowired
	private ClientService clientService;

	@GetMapping("/myAccount")
	public ResponseEntity<AccountReadDto> getAccountDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsDetector userDetailsDetector = new UserDetailsDetector();
		ResponseEntity<AccountReadDto> responseAccount = ResponseEntity.noContent().build();

		if (userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
			long clientId = userDetailsDetector.getClientId();

			AccountReadDto mayBeAccount = accountService.readAccountByClientId(clientId);
			if(mayBeAccount != null){
				responseAccount = ResponseEntity.ok().body(mayBeAccount);
			}
		}
		return responseAccount;
	}
}
