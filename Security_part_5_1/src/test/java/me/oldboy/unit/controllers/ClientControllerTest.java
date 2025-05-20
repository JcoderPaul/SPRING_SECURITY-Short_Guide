package me.oldboy.unit.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import me.oldboy.test_content.TestConstantFields;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/* Проверяем только логику - "изолированные" unit тесты, используя Mockito (без Spring context и поддержки Spring Security test) */
class ClientControllerTest {

    /* Для тестирования методов getAllClient() и  registrationClient()*/
    @Mock
    private ClientService mockClientService;
    @Mock
    private BindingResult mockBindingResult;

    /* Для тестирования метода getAdminName() */
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private UserDetails mockUserDetails;

    /* Тестируемый класс */
    @InjectMocks
    private ClientController clientController;

    private static List<ClientReadDto> testDtoList;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto;
    private ClientReadDto testClientReadDto;
    private Client testClient;
    private Details testDetails;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setStaticContent(){
        testDtoList = new ArrayList<>(List.of(new ClientReadDto(),
                                              new ClientReadDto(),
                                              new ClientReadDto()));

        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testClientReadDto = new ClientReadDto(TestConstantFields.TEST_EMAIL, Role.USER.name(), TestConstantFields.TEST_CLIENT_NAME, TestConstantFields.TEST_CLIENT_SUR_NAME, TestConstantFields.TEST_AGE);
        testDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME, TestConstantFields.TEST_CLIENT_SUR_NAME, TestConstantFields.TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL, TestConstantFields.TEST_PASS, testDetailsCreateDto);
        notValidCreateClientDto = new ClientCreateDto("sw","2", testDetailsCreateDto);

        testDetails = Details.builder()
                .clientName(TestConstantFields.TEST_CLIENT_NAME)
                .clientSurName(TestConstantFields.TEST_CLIENT_SUR_NAME)
                .age(TestConstantFields.TEST_AGE)
                .client(testClient)
                .build();

        testClient = Client.builder()
                .id(1L)
                .email(TestConstantFields.TEST_EMAIL)
                .pass(TestConstantFields.TEST_PASS)
                .role(Role.USER)
                .details(testDetails)
                .build();
    }

    @Test
    void shouldReturnWelcomeStringAdminNameTest() {
        /* Задаем поведение mock "заглушек" */
        when(mockUserDetails.getUsername()).thenReturn(TestConstantFields.TEST_EMAIL);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);

        String result = clientController.getAdminName(mockAuthentication); // Вызываем тестируемый метод

        String expected = "This page for ADMIN only! \nHello: " + TestConstantFields.TEST_EMAIL;
        assertThat(result).isEqualTo(expected); // Сравниваем результат и ожидание
    }

    @Test
    void checkCurrentSizeOfTestedListGetAllClientTest() {
        when(mockClientService.findAll()).thenReturn(testDtoList);
        int testListSize = testDtoList.size();

        List<ClientReadDto> listFromController = clientController.getAllClient();
        int expectedListSize = listFromController.size();

        assertThat(testListSize).isEqualTo(expectedListSize);
    }

    /*
    При тестировании метода *.registrationClient() нам нужно покрыть минимум 3-и сценария:
    - успешная регистрация;
    - дублирование e-mail при регистрации;
    - не валидный CreateClientDto;
    */

    @Test
    @SneakyThrows
    void checkOkMethodLogicRegistrationClientTest() {
        when(mockClientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.empty());
        when(mockClientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<String> methodAnswer = clientController.registrationClient(testClientCreateDto,mockBindingResult);
        assertThat(methodAnswer.getStatusCode().is2xxSuccessful()).isTrue();

        String expectedBody = objectMapper.writer()
                                          .withDefaultPrettyPrinter()
                                          .writeValueAsString(testClientReadDto);
        assertThat(methodAnswer.getBody()).isEqualTo(expectedBody);

        verify(mockClientService, times(1)).findByEmail(anyString());
        verify(mockClientService, times(1)).saveClient(any(ClientCreateDto.class));
        verify(mockBindingResult, times(1)).hasErrors();
    }

    @Test
    void shouldReturnDuplicateEmailExceptionRegistrationClientTest() {
        when(mockClientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.of(testClient));
        when(mockBindingResult.hasErrors()).thenReturn(false);

        assertThatThrownBy(() -> clientController.registrationClient(testClientCreateDto, mockBindingResult))
                .isInstanceOf(DuplicateClientEmailException.class)
                .hasMessageContaining("Email: " + testClientCreateDto.email() + " is exist.");

        verify(mockClientService, times(1)).findByEmail(anyString());
        verify(mockBindingResult, times(1)).hasErrors();
    }

    @Test
    void shouldReturn_ValidationErrors_RegistrationClientTest() throws JsonProcessingException {
        /* Формируем массив потенциально известных ошибок */
        List<ObjectError> errors = Arrays.asList(
                new FieldError("clientCreateDto", "pass","Field size value can be between 2 and 64"),
                new FieldError("clientCreateDto","email","Standard e-mail structure - email_name@email_domain.top_lavel_domain")
        );

        /* Mock-аем работу методов BindingResult */
        when(mockBindingResult.hasErrors()).thenReturn(true);
        when(mockBindingResult.getAllErrors()).thenReturn(errors);

        /* Вызываем тестируемый метод */
        ResponseEntity<String> response = clientController.registrationClient(notValidCreateClientDto, mockBindingResult);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()); // Проверяем статус ответа от метода

        /* Превращаем коллекцию ошибок в коллекцию сообщений об ошибке, для конвертации в текст (Json формата) */
        List<String> expectedErrors = errors.stream()
                .map(objectError -> objectError.getDefaultMessage())
                .collect(Collectors.toList());

        /* Конвертируем полученную ранее коллекцию в "Json ответ" */
        String expectedJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(expectedErrors);
        assertEquals(expectedJson, response.getBody()); // Сравниваем тело ответа тестируемого метода с тестовой коллекцией

        verify(mockBindingResult, times(1)).hasErrors();
    }
}