package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import org.springframework.security.access.annotation.Secured;
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

	/* Методы внедренные ниже предоставлены исключительно в целях ознакомления с Method Security и не обладают должными проверками и т.д. */


	/*
	Пусть у нас кредит может взять только сам клиент, сделаем минимальные проверки,
	используем @PreAuthorize в котором сравним значение clientId из переданного DTO
	и значением ID клиента из SecurityClientDetails нашего наследника UserDetails,
	поскольку мы добавили в него возможность извлекать Client-a, а значит и его ID.

	В случае совпадения ID из обоих аргументов доступ к методу будет получен, если
	значения ID не совпали - "Access Denied"
	*/
	@PreAuthorize("#loanCreateDto.clientId == #userDetails.client.id")
	@PostMapping("/createLoan")
	public ResponseEntity<?> createLoan(@Param("loanCreateDto") @RequestBody LoanCreateDto loanCreateDto,
										@Param("userDetails") @AuthenticationPrincipal SecurityClientDetails userDetails){

		/*
		В качестве теста, 'чего же там парсит SpEL' применим простой код и посмотрим в debug-e:

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext(loanCreateDto);
		StandardEvaluationContext context_2 = new StandardEvaluationContext(userDetails);

		// Извлечение ID из аргументов
		Long lcdId = parser.parseExpression("clientId").getValue(context, Long.class);
		Long usdId = parser.parseExpression("client.id").getValue(context_2, Long.class);
		*/

		String userEmail = userDetails.getUsername();
		Long newLoanId = null;

		Optional<Client> mayBeClient = clientService.findByEmail(userEmail);
		if(mayBeClient.isPresent()){
			loanCreateDto.setClientId(mayBeClient.get().getId());
			newLoanId = loanService.saveLoan(loanCreateDto);
		}
		return ResponseEntity.ok().body(newLoanId);
	}

	/*
	Тут мы применяем @PreFilter при этом у нас не один аргумент в виде коллекции, как показано в примерах, а два
	и нам нужно явно их выделить, как в методе, через @Param, так и самое важное в самой @PreFilter. Явно указываем
	filterTarget = "dtoList" - что будем фильтровать и value = "... условие как будем фильтровать ...".

	В нашем примере, мы засылаем через Postman JSON коллекцию LoanCreateDto-ек и хотим, чтобы в теле метода оказалась
	коллекция кредитов-loan у которой элементы с clientId аутентифицированного клиента, а все остальные были отсеяны,
	т.е. аннотация @PreFilter в данной конфигурации не дает передать в метод "чужие кредиты", только залогиненного
	клиента.
	*/
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

	/*
	Применяем @PostFilter, который должен согласно переданным в аннотацию условиям обработать (докрутить до кондиций)
	результат работы метода, у нас это возвращаемое значение - List<LoanReadDto>. Т.е. без данной аннотации, мы бы,
	согласно работе метода *.findAll() слоя сервисов получили бы все записи таблицы loans из БД. Но мы решили, что
	данный фильтр нам подходит и мы хотим получить только записи с точным указанием "типа кредита" - на что взят кредит.
	У нас несколько видов (см. loans_scripts.sql поле loan_type). Через переменную пути запроса - "совсем не красиво" -
	но мы изучаем, передаем один из возможных типов и видим результат фильтра в виде JSON объекта (коллекции).
	*/
	@Secured("ROLE_ADMIN") // Эта аннотация из раздела о защите по правам доступа, теперь метод могут использовать только ADMIN-ы
	@PostFilter("filterObject.loanType == #loanType")
	@GetMapping("/get-all-loans-by-type/{loanType}")
	public List<LoanReadDto> getAllLoanByType(@Param("loanType") @PathVariable("loanType") String loanType){
		return loanService.findAll();
	}
}