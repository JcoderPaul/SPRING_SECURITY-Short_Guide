package me.oldboy.controllers.api;

import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.loan_dto.LoanCreateDto;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import me.oldboy.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoansController {

	@Autowired
	private final ClientService clientService;
	@Autowired
	private final LoanService loanService;
	@Autowired
	private final UserDetailsDetector userDetailsDetector;

	@GetMapping("/myLoans")
	public ResponseEntity<List<LoanReadDto>> getLoanDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		ResponseEntity<List<LoanReadDto>> loansList = ResponseEntity.noContent().build();

		Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService,authentication);
		if(mayBeClient.isPresent()){
			long clientId = mayBeClient.get().getId();

			List<LoanReadDto> mayBeLoans = loanService.readAllLoansByUserId(clientId);
			if(mayBeLoans.size() != 0){
				loansList = ResponseEntity.ok().body(mayBeLoans);
			}
		}
		return loansList;
	}

	@PreAuthorize("#loanCreateDto.clientId == #userDetails.client.id")
	@PostMapping("/createLoan")
	public ResponseEntity<?> createLoan(@Param("loanCreateDto") @RequestBody LoanCreateDto loanCreateDto,
										@Param("userDetails") @AuthenticationPrincipal SecurityClientDetails userDetails){

		String userEmail = userDetails.getUsername();
		Long newLoanId = null;

		Optional<Client> mayBeClient = clientService.findByEmail(userEmail);
		if(mayBeClient.isPresent()){
			loanCreateDto.setClientId(mayBeClient.get().getId());
			newLoanId = loanService.saveLoan(loanCreateDto);
		}
		return ResponseEntity.ok().body(newLoanId);
	}

	@PreFilter(filterTarget = "dtoList", value = "filterObject.clientId == #userDetails.client.id")
	@PostMapping("/save-all-loans")
	public ResponseEntity<String> saveAllMyRequestLoan(@Param("dtoList") @RequestBody List<LoanCreateDto> dtoList,
									              @Param("userDetails") @AuthenticationPrincipal SecurityClientDetails userDetails){

		String prnResponseAsSting = "Operation is failed!";
		if(loanService.saveAllMyLoans(dtoList, userDetails)){
			prnResponseAsSting = "Saved all loans!";
		}

		return ResponseEntity.ok().body(prnResponseAsSting);
	}

	@RolesAllowed("ADMIN") // Эта аннотация из раздела о защите по правам доступа, теперь метод могут использовать только ADMIN-ы
	@PostFilter("filterObject.loanType == #loanType")
	@GetMapping("/get-all-loans-by-type/{loanType}")
	public List<LoanReadDto> getAllLoanByType(@Param("loanType") @PathVariable("loanType") String loanType){
		List<LoanReadDto> returnDtoList = loanService.findAll();
		return returnDtoList;
	}
}