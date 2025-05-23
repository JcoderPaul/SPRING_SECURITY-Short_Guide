package me.oldboy.unit.controllers.cc_test_config_mod;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/* Проверяем только логику - "изолированные" unit тесты, используя только Mockito - 5 сек */
class ClientControllerOnlyMockTest {

    /* Для тестирования методов getAllClient() и  registrationClient()*/
    @Mock
    private ClientService mockClientService;

    /* Для тестирования метода getAdminName() */
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private UserDetails mockUserDetails;
    @Mock
    private BindingResult mockBindingResult;

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
        testDtoList = new ArrayList<>(List.of(new ClientReadDto(), new ClientReadDto(),  new ClientReadDto()));

        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testClientReadDto = new ClientReadDto(TEST_EMAIL, Role.USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
        notValidCreateClientDto = new ClientCreateDto("","", testDetailsCreateDto);

        testDetails = Details.builder()
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .client(testClient)
                .build();

        testClient = Client.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .details(testDetails)
                .build();
    }

    @Test
    void shouldReturnWelcomeStringAdminNameTest() {
        /* Задаем поведение mock "заглушек" */
        when(mockUserDetails.getUsername()).thenReturn(TEST_EMAIL);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);

        String result = clientController.getAdminName(mockAuthentication); // Вызываем тестируемый метод

        String expected = "This page for ADMIN only! \nHello: " + TEST_EMAIL;
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

    @Test
    @SneakyThrows
    void checkOkMethodLogicRegistrationClientTest() {
        when(mockClientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.empty());
        when(mockClientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);

        ResponseEntity<String> methodAnswer = clientController.registrationClient(testClientCreateDto, mockBindingResult);
        assertThat(methodAnswer.getStatusCode().is2xxSuccessful()).isTrue();

        String expectedBody = objectMapper.writer()
                .withDefaultPrettyPrinter()
                .writeValueAsString(testClientReadDto);
        assertThat(methodAnswer.getBody()).isEqualTo(expectedBody);

        verify(mockClientService, times(1)).findByEmail(anyString());
        verify(mockClientService, times(1)).saveClient(any(ClientCreateDto.class));
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

    @Test
    void shouldReturnDuplicateEmailExceptionRegistrationClientTest() {
        when(mockClientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.of(testClient));

        assertThatThrownBy(() -> clientController.registrationClient(testClientCreateDto,mockBindingResult))
                .isInstanceOf(DuplicateClientEmailException.class)
                .hasMessageContaining("Email: " + testClientCreateDto.email() + " is exist.");

        verify(mockClientService, times(1)).findByEmail(anyString());
    }
}
