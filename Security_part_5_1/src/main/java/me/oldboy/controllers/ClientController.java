package me.oldboy.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/admin") // Задаем общий префикс для всех эндпоинтов этого контроллера
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/helloAdmin")
    public String getAdminName(Authentication authentication) {
        UserDetails clientDetails = (UserDetails) authentication.getPrincipal();

        return "This page for ADMIN only! \n" +
               "Hello: " + clientDetails.getUsername(); // Выводим имя аутентифицированного клиента
    }

    @GetMapping("/getAllClient")
    public List<ClientReadDto> getAllClient(){
        return clientService.findAll();
    }

    /*
    Хотя в предыдущем проекте мы сократили код в этом методе за счет
    использования глобального обработчика и переноса логики проверки
    туда, что в дальнейшем позволит использовать его и для других DTO
    тут все оставим без изменений.
    */
    @PostMapping("/regClient")
    public ResponseEntity<String> registrationClient(@Validated
                                                     @RequestBody
                                                     ClientCreateDto clientCreateDto,
                                                     BindingResult bindingResult) throws JsonProcessingException {
        if (bindingResult.hasErrors()){
            List<String> errorsMessage = bindingResult.getAllErrors().stream()
                    .map(errors -> errors.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new ObjectMapper().writer()
                    .withDefaultPrettyPrinter()
                    .writeValueAsString(errorsMessage));
        }

        if(clientService.findByEmail(clientCreateDto.email()).isPresent()){
            throw new DuplicateClientEmailException("Email: " + clientCreateDto.email() + " is exist.");
        } else {
            return ResponseEntity.ok(new ObjectMapper().writer()
                                 .withDefaultPrettyPrinter()
                                 .writeValueAsString(clientService.saveClient(clientCreateDto)));
        }
    }
}
