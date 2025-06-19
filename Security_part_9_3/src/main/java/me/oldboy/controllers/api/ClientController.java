package me.oldboy.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.constants.SecurityConstants;
import me.oldboy.dto.auth_dto.ClientAuthRequest;
import me.oldboy.dto.auth_dto.ClientAuthResponse;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.exception.EmptyCurrentClientException;
import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/regClient")
    public ResponseEntity<?> registrationClient(@Validated
                                                @RequestBody
                                                ClientCreateDto clientCreateDto,
                                                BindingResult bindingResult) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            return validateRequestEntity(bindingResult);
        }

        if(clientService.findByEmail(clientCreateDto.getEmail()).isPresent()){
            throw new DuplicateClientEmailException("Email: " + clientCreateDto.getEmail() + " is exist, can not duplicate data.");
        } else {
            return ResponseEntity.ok().body(clientService.saveClient(clientCreateDto));
        }
    }

    @PostMapping("/loginClient")
    public ResponseEntity<?> loginClient(@Validated
                                         @RequestBody
                                         ClientAuthRequest clientAuthRequest,
                                         BindingResult bindingResult,
                                         HttpServletResponse response) throws JsonProcessingException {

        if (bindingResult.hasErrors()) {
            return validateRequestEntity(bindingResult);
        }

        ClientAuthResponse clientAuthResponse = new ClientAuthResponse();
        Optional<Client> mayByClient = clientService.getClientIfAuthDataCorrect(clientAuthRequest);

        if(mayByClient.isPresent()) {
            String jwtResponseToken = response.getHeader(SecurityConstants.JWT_HEADER);

            clientAuthResponse.setId(mayByClient.get().getId());
            clientAuthResponse.setClientLogin(mayByClient.get().getEmail());
            clientAuthResponse.setAccessToken(jwtResponseToken);
        }

        return ResponseEntity.ok().body(clientAuthResponse);
    }

    private static ResponseEntity<?> validateRequestEntity(BindingResult bindingResult) throws JsonProcessingException {
            List<String> errorsMessage = bindingResult.getAllErrors().stream()
                    .map(errors -> errors.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new ObjectMapper().writer()
                    .withDefaultPrettyPrinter()
                    .writeValueAsString(errorsMessage));
    }
}