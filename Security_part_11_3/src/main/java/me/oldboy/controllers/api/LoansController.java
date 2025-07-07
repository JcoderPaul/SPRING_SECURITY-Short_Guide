package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.loan_dto.LoanCreateDto;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import me.oldboy.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api")
public class LoansController {

	@Autowired
	private ClientService clientService;
	@Autowired
	private LoanService loanService;
	@Autowired
	private UserDetailsDetector userDetailsDetector;
	
	@GetMapping("/myLoans")
	public ResponseEntity<List<LoanReadDto>> getLoanDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		ResponseEntity<List<LoanReadDto>> loansList = ResponseEntity.noContent().build();

		Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService, authentication);

		if(mayBeClient.isPresent()){
			long clientId = mayBeClient.get().getId();

			List<LoanReadDto> mayBeLoans = loanService.readAllLoansByUserId(clientId);
			if(mayBeLoans.size() != 0){
				loansList = ResponseEntity.ok().body(mayBeLoans);
			}
		}
		return loansList;
	}

	@PostMapping("/createLoan")
	public ResponseEntity<?> createLoan(@Param("loanCreateDto") @RequestBody LoanCreateDto loanCreateDto) {
		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		Long newLoanId = null;

		Optional<Client> mayBeClient = clientService.findByEmail(userEmail);
		if(mayBeClient.isPresent()){
			if(mayBeClient.get().getId() == loanCreateDto.getClientId()){
				newLoanId = loanService.saveLoan(loanCreateDto);
				return ResponseEntity.ok().body(newLoanId);
			}
		}
		return ResponseEntity.badRequest().body("");
	}

	@PostMapping("/save-all-loans")
	public ResponseEntity<String> saveAllMyRequestLoan(@Param("dtoList") @RequestBody List<LoanCreateDto> dtoList){
		String prnResponseAsSting = "Operation is failed!";
		List<LoanCreateDto> toSaveList = null;

		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<Client> mayBeClient = clientService.findByEmail(userEmail);

		if(mayBeClient.isPresent()){
			long clientId = mayBeClient.get().getId();
			toSaveList = dtoList.stream()
					.filter(loanCreateDto -> loanCreateDto.getClientId() == clientId)
					.toList();
		}

		if(loanService.saveAllMyLoans(toSaveList)){
			prnResponseAsSting = "Saved all loans!";
		}

		return ResponseEntity.ok().body(prnResponseAsSting);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/get-all-loans-by-type/{loanType}")
	public List<LoanReadDto> getAllLoanByType(@Param("loanType") @PathVariable("loanType") String loanType){
		List<LoanReadDto> getLoansFromBase = loanService.findAll();
		return getLoansFromBase.stream()
				.filter(loanReadDto -> loanReadDto.getLoanType().equals(loanType))
				.collect(Collectors.toList());
	}
}