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
import me.oldboy.test_content.TestFields;
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
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


class ClientControllerTest {

    /* Для тестирования методов getAllClient() и  registrationClient()*/
    @Mock
    private ClientService mockClientService;
    @Mock
    private BindingResult mockBindingResult;

    /* Для тестирования метода getAdminName() */
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityClientDetails clientDetails;

    @InjectMocks
    private ClientController clientController;

    private static List<ClientReadDto> testDtoList;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto;
    private ClientReadDto testClientReadDto;
    private Client testClient;
    private Details testDetails;

    @BeforeAll
    static void setStaticContent(){
        testDtoList = new ArrayList<>(List.of(new ClientReadDto(),
                                              new ClientReadDto(),
                                              new ClientReadDto()));
    }

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testClientReadDto = new ClientReadDto(TestFields.TEST_EMAIL,
                                              Role.USER.name(),
                                              TestFields.TEST_CLIENT_NAME,
                                              TestFields.TEST_CLIENT_SUR_NAME,
                                              TestFields.TEST_AGE);

        testDetailsCreateDto = new DetailsCreateDto(TestFields.TEST_CLIENT_NAME,
                                                    TestFields.TEST_CLIENT_SUR_NAME,
                                                    TestFields.TEST_AGE);

        testClientCreateDto = new ClientCreateDto(TestFields.TEST_EMAIL,
                                                  TestFields.TEST_PASS,
                                                  testDetailsCreateDto);

        testDetails = Details.builder()
                            .clientName(TestFields.TEST_CLIENT_NAME)
                            .clientSurName(TestFields.TEST_CLIENT_SUR_NAME)
                            .age(TestFields.TEST_AGE)
                            .client(testClient)
                            .build();

        testClient = Client.builder()
                            .id(1L)
                            .email(TestFields.TEST_EMAIL)
                            .pass(TestFields.TEST_PASS)
                            .role(Role.USER)
                            .details(testDetails)
                            .build();
    }

    @Test
    void shouldReturnWelcomeStringAdminNameTest() {
        /* Задаем поведение mock "заглушек" */
        when(clientDetails.getClientName()).thenReturn(TestFields.TEST_CLIENT_NAME);
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext); // Задаем SecurityContext mock
        String result = clientController.getAdminName(); // Вызываем тестируемый метод

        String expected = "This page for ADMIN only! \nHello: " + TestFields.TEST_CLIENT_NAME;
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
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<String> methodAnswer = clientController.registrationClient(testClientCreateDto,mockBindingResult);
        assertThat(methodAnswer.getStatusCode().is2xxSuccessful()).isTrue();

        String expectedBody = new ObjectMapper().writer()
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
}