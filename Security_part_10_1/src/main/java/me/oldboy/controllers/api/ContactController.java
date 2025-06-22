package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.mapper.ContactMapper;
import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import me.oldboy.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
public class ContactController {

	@Autowired
	private ClientService clientService;
	@Autowired
	private ContactService contactService;
	
	@GetMapping("/myContact")
	public ResponseEntity<ContactReadDto> getContactDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsDetector userDetailsDetector = new UserDetailsDetector();
		ResponseEntity<ContactReadDto> clientContact = ResponseEntity.noContent().build();

		if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
			long clientId = userDetailsDetector.getClientId();

			Optional<ContactReadDto> mayBeContact = contactService.readContact(clientId);
			if(mayBeContact.isPresent()){
				clientContact = ResponseEntity.ok().body(mayBeContact.get());
			}
		}
		return clientContact;
	}

	/*
	Предикаты, как уже упоминалось в теории ReadMe.md, можно писать с помощью SpEL (Spring Expression Language). Т.е. у
	нас может быть написано достаточно сложное выражение например:

		@PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR')")
		public boolean getMyMethod() {
			//...
		}

	Более того, мы можем использовать аргументы метода как часть выражения :

		@PreAuthorize("#username == authentication.principal.username")
		public String getMyMethod(String username) {
			//...
		}

	Здесь пользователь может вызывать метод *.getMyMethod() только в том случае, если значение аргумента username совпадает
	с именем пользователя текущего принципала. Но, тут мы применим простую проверку роли авторизованного клиента:
	*/

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/user-contacts")
	public ResponseEntity<List<ContactReadDto>> getAllContacts() {

		List<ContactReadDto> readAllContacts =
				contactService.readAllContacts().stream()
						.map(ContactMapper.INSTANCE::mapToContactReadDto)
						.toList();

		return ResponseEntity.ok().body(readAllContacts);
	}

	/*
	Умышленно создадим ситуацию при которой удачно залогиненный пользователь не сможет воспользоваться результатами
	метода. В @PostAuthorize пропишем условие, при котором будем сравнивать название города аутентифицированного
	клиента и заранее заданного, у нас это "Ufa". А поскольку ни у одного из наших клиентов в БД такого города в
	контактах в поле city нет, то метод бросит: {"exceptionMsg":"Access Denied"}.

	Но если в параметрах задать город аутентифицированного пользователя, то мы получим его данные в ответ.

	Интересный момент, неожиданный, но уже встречаемый ранее - проблемы при распознавании аргументов метода в non
	Boot Spring приложении. В данном случае аргумент myCity не хотел распознаваться и SpEL выражение в аннотации не
	парсилось должным образом - мы ловили ошибку (либо получали отказ в допуске), проблему решила аннотация @Param.
	*/
	@PostAuthorize("#myCity == returnObject.city")
	@GetMapping("/user-contact-with-condition/{myCity}")
	public ContactReadDto getContactsIfConditionIsGood(@Param("myCity") @PathVariable("myCity") String myCity,
					   			@AuthenticationPrincipal UserDetails userDetails) {
		String userName = userDetails.getUsername();
		ContactReadDto readContact = null;

		Optional<Client> mayBeClient = clientService.findByEmail(userName);
		if(mayBeClient.isPresent()) {
			Long clientId = mayBeClient.get().getId();
			Optional<ContactReadDto> contact = contactService.readContact(clientId);
			readContact = contact.get();
		}
		return readContact;
	}
}