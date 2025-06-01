package me.oldboy.controllers.api.cc_tests_kinds;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.api.ClientController;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ClientControllerOnlyMockTest {

    /* Для тестирования метода getAdminName() */
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;

    /* Для тестирования методов getAllClient() и  registrationClient()*/
    @Mock
    private ClientService mockClientService;
    @Mock
    private BindingResult mockBindingResult;
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

        testClientReadDto = new ClientReadDto(TEST_EMAIL, Role.ROLE_USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
        notValidCreateClientDto = new ClientCreateDto("we","2", testDetailsCreateDto);

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
                .role(Role.ROLE_USER)
                .details(testDetails)
                .build();
    }

    @Test
    void shouldReturnWelcomeStringAdminNameTest() {
        /* Задаем поведение mock "заглушек" */
        when(mockClientDetails.getUsername()).thenReturn(TEST_CLIENT_NAME);
        when(mockAuthentication.getPrincipal()).thenReturn(mockClientDetails);

        String result = clientController.getAdminName(mockAuthentication); // Вызываем тестируемый метод

        String expected = "This page for ADMIN only! \nHello: " + TEST_CLIENT_NAME;
        assertThat(result).isEqualTo(expected); // Сравниваем результат и ожидание

        verify(mockClientDetails, times(1)).getUsername();
        verify(mockAuthentication, times(1)).getPrincipal();
    }

    @Test
    void checkCurrentSizeOfTestedListGetAllClientTest() {
        when(mockClientService.findAll()).thenReturn(testDtoList);
        int testListSize = testDtoList.size();

        List<ClientReadDto> listFromController = clientController.getAllClient();
        int expectedListSize = listFromController.size();

        assertThat(testListSize).isEqualTo(expectedListSize);

        verify(mockClientService, times(1)).findAll();
    }

    /*
        В последних двух тестовых методах мы тестировали только логику самих методов, а вот
        BindingResult тут только использовался в виде "заглушки" и фактически не отрабатывал
        положенную логику валидации входящих DTO данных, вернее так, любые данные поданные на
        вход были бы "валидными". Его работу мы будем проверять в интеграционных тестах.
    */
    @Test
    @SneakyThrows
    void checkOkMethodLogicRegistrationClientTest() {
        when(mockClientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.empty());
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
    void shouldReturnDuplicateEmailExceptionRegistrationClientTest() {
        when(mockClientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.of(testClient));

        assertThatThrownBy(() -> clientController.registrationClient(testClientCreateDto, mockBindingResult))
                .isInstanceOf(DuplicateClientEmailException.class)
                .hasMessageContaining("Email: " + testClientCreateDto.getEmail() + " is exist.");

        verify(mockClientService, times(1)).findByEmail(anyString());
    }
}