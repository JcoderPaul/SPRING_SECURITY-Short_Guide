package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.services.BalanceService;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api")
public class BalanceController {

	@Autowired
	private BalanceService balanceService;
	@Autowired
	private ClientService clientService;
	@Autowired
	private UserDetailsDetector userDetailsDetector;
	
	@GetMapping("/myBalance")
	public ResponseEntity<List<TransactionReadDto>> getBalanceDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		ResponseEntity<List<TransactionReadDto>> balanceList = ResponseEntity.noContent().build();

		if (userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
			long clientId = userDetailsDetector.getClientId();

			List<TransactionReadDto> mayBeTransactions = balanceService.readAllTransactionByClientId(clientId);
			if (mayBeTransactions.size() != 0) {
				balanceList = ResponseEntity.ok().body(mayBeTransactions);
			}
		}
		return balanceList;
	}
}
