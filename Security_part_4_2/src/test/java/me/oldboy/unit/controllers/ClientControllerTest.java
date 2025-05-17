package me.oldboy.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import me.oldboy.test_config.TestConstantFields;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/* Тут мы проверяем логику методов "изолированно" */
class ClientControllerTest {

    /* Для тестирования метода getAdminName() */
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;

    /* Для тестирования методов getAllClient() и  registrationClient()*/
    @Mock
    private ClientService mockClientService;
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

        testClientReadDto = new ClientReadDto(TestConstantFields.TEST_EMAIL,
                                              Role.USER.name(),
                                              TestConstantFields.TEST_CLIENT_NAME,
                                              TestConstantFields.TEST_CLIENT_SUR_NAME,
                                              TestConstantFields.TEST_AGE);

        testDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME,
                                                    TestConstantFields.TEST_CLIENT_SUR_NAME,
                                                    TestConstantFields.TEST_AGE);

        testClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL,
                                                  TestConstantFields.TEST_PASS,
                                                  testDetailsCreateDto);

        notValidCreateClientDto = new ClientCreateDto("","", testDetailsCreateDto);

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
        when(mockClientDetails.getClientName()).thenReturn(TestConstantFields.TEST_CLIENT_NAME);
        when(mockAuthentication.getPrincipal()).thenReturn(mockClientDetails);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);

        SecurityContextHolder.setContext(mockSecurityContext); // Задаем SecurityContext mock
        String result = clientController.getHelloAdmin(); // Вызываем тестируемый метод

        String expected = "This page for ADMIN only! \nHello: " + TestConstantFields.TEST_CLIENT_NAME;
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

        ResponseEntity<String> methodAnswer = clientController.registrationClient(testClientCreateDto);
        assertThat(methodAnswer.getStatusCode().is2xxSuccessful()).isTrue();

        String expectedBody = objectMapper.writer()
                                          .withDefaultPrettyPrinter()
                                          .writeValueAsString(testClientReadDto);
        assertThat(methodAnswer.getBody()).isEqualTo(expectedBody);

        verify(mockClientService, times(1)).findByEmail(anyString());
        verify(mockClientService, times(1)).saveClient(any(ClientCreateDto.class));
    }

    @Test
    void shouldReturnDuplicateEmailExceptionRegistrationClientTest() {
        when(mockClientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.of(testClient));

        assertThatThrownBy(() -> clientController.registrationClient(testClientCreateDto))
                .isInstanceOf(DuplicateClientEmailException.class)
                .hasMessageContaining("Email: " + testClientCreateDto.email() + " is exist.");

        verify(mockClientService, times(1)).findByEmail(anyString());
    }
}