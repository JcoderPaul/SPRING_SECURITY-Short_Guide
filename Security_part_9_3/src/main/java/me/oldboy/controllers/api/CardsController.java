package me.oldboy.controllers.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.services.CardService;
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
public class CardsController {

	@Autowired
	private ClientService clientService;
	@Autowired
	private CardService cardService;
	@Autowired
	private UserDetailsDetector userDetailsDetector;
	
	@GetMapping("/myCards")
	public ResponseEntity<List<CardReadDto>> getCardDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		ResponseEntity<List<CardReadDto>> clientCards = ResponseEntity.noContent().build();

		if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
			long clientId = userDetailsDetector.getClientId();

			List<CardReadDto> mayBeCards = cardService.findAllCardsByClientId(clientId);
			if (mayBeCards.size() != 0) {
				clientCards = ResponseEntity.ok().body(mayBeCards);
			}
		}
		return clientCards;
	}
}