package me.oldboy.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/admin") // Задаем общий префикс для всех эндпоинтов этого контроллера
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/helloAdmin")
    public String getClientList() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityClientDetails clientDetails = (SecurityClientDetails) authentication.getPrincipal();

        return "This page for ADMIN only! \n" +
               "Hello: " + clientDetails.getClientName(); // Выводим имя аутентифицированного клиента
    }

    @GetMapping("/getAllClient")
    public List<ClientReadDto> getAllClient(){
        return clientService.findAll();
    }

    @PostMapping("/regClient")
    public ResponseEntity<String> registrationClient(@Valid
                                                     @RequestBody
                                                     ClientCreateDto clientCreateDto) throws JsonProcessingException {

        if(clientService.findByEmail(clientCreateDto.email()).isPresent()){
            throw new DuplicateClientEmailException("Email: " + clientCreateDto.email() + " is exist.");
        } else {
            return ResponseEntity.ok(new ObjectMapper().writer()
                                 .withDefaultPrettyPrinter()
                                 .writeValueAsString(clientService.saveClient(clientCreateDto)));
        }
    }
}