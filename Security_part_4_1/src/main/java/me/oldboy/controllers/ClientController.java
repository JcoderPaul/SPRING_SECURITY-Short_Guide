package me.oldboy.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public String getAdminName() {

        /*
        Очень интересный код, тут мы связываем заданный SecurityContext с текущим Java потоком выполнения:
        1. Authentication - представляет токен для запроса аутентификации или для аутентифицированного
        принципала после обработки запроса. В нем содержится информация об аутентифицированном клиенте
        и эту информацию мы хотим тут получить.
        2. После аутентификации запроса Authentication обычно сохраняется в локальном потоке SecurityContext,
        управляемом SecurityContextHolder с помощью используемого механизма аутентификации. Значит отсюда мы
        его и можем получить.
        3. Получаем принципала из объекта аутентификации, т.к. реализация AuthenticationManager часто возвращает
        Authentication, содержащую более подробную информацию в качестве принципала для использования приложением.
        Многие поставщики аутентификации будут создавать объект UserDetails в качестве принципала, что нам и надо.
        4. Получаем данные аутентифицированного клиента. В нашем классе SecurityClientDetails наследнике UserDetails
        есть самописный метод *.getClientName() вот его мы и применяем для получения имени. Нам никто не мешал получить
        всего клиента в этом же или другом методе.
        */
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
