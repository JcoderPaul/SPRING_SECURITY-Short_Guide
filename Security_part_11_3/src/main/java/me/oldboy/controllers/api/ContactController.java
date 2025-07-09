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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api")
public class ContactController {

	@Autowired
	private ClientService clientService;
	@Autowired
	private ContactService contactService;
	@Autowired
	private UserDetailsDetector userDetailsDetector;
	
	@GetMapping("/myContact")
	public ResponseEntity<ContactReadDto> getContactDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		ResponseEntity<ContactReadDto> clientContact = ResponseEntity.noContent().build();

		Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService, authentication);

		if(mayBeClient.isPresent()) {
			long clientId = mayBeClient.get().getId();

			Optional<ContactReadDto> mayBeContact = contactService.readContact(clientId);
			if(mayBeContact.isPresent()){
				clientContact = ResponseEntity.ok().body(mayBeContact.get());
			}
		}
		return clientContact;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/user-contacts")
	public ResponseEntity<List<ContactReadDto>> getAllContacts() {

		List<ContactReadDto> readAllContacts =
				contactService.readAllContacts().stream()
							                    .map(ContactMapper.INSTANCE::mapToContactReadDto)
							                    .collect(Collectors.toList());

		return ResponseEntity.ok().body(readAllContacts);
	}

	@GetMapping("/user-contact-with-condition/{myCity}")
	public ResponseEntity<ContactReadDto> getContactsIfConditionIsGood(@Param("myCity") @PathVariable("myCity") String myCity) {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		ContactReadDto readContact = null;

		Optional<Client> mayBeClient = clientService.findByEmail(userName);
		if(mayBeClient.isPresent()) {
			Long clientId = mayBeClient.get().getId();
			Optional<ContactReadDto> contact = contactService.readContact(clientId);
			if(contact.isPresent()){
				if(contact.get().getCity().equals(myCity)){
					readContact = contact.get();
					return ResponseEntity.ok().body(readContact);
				}
			}
		}
		return ResponseEntity.badRequest().body(readContact);
	}
}