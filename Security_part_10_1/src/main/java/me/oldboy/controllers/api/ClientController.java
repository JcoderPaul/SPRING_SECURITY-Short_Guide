package me.oldboy.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
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

    private static ResponseEntity<?> validateRequestEntity(BindingResult bindingResult) throws JsonProcessingException {
            List<String> errorsMessage = bindingResult.getAllErrors().stream()
                    .map(errors -> errors.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new ObjectMapper().writer()
                    .withDefaultPrettyPrinter()
                    .writeValueAsString(errorsMessage));
    }
}