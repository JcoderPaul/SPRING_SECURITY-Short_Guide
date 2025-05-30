package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.services.ClientService;
import me.oldboy.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}