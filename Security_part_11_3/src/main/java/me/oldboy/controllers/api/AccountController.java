package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.services.AccountService;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api")
public class AccountController {

	@Autowired
	private AccountService accountService;
	@Autowired
	private ClientService clientService;
	@Autowired
	private UserDetailsDetector userDetailsDetector;

	@GetMapping("/myAccount")
	public ResponseEntity<AccountReadDto> getAccountDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		ResponseEntity<AccountReadDto> responseAccount = ResponseEntity.noContent().build();

		Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService, authentication);

		if (mayBeClient.isPresent()) {
			long clientId = mayBeClient.get().getId();

			AccountReadDto mayBeAccount = accountService.readAccountByClientId(clientId);
			if(mayBeAccount != null){
				responseAccount = ResponseEntity.ok().body(mayBeAccount);
			}
		}
		return responseAccount;
	}
}