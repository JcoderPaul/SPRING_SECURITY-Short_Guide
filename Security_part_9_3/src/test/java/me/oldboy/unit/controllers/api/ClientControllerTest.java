package me.oldboy.unit.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.controllers.api.ClientController;
import me.oldboy.dto.auth_dto.ClientAuthRequest;
import me.oldboy.dto.auth_dto.ClientAuthResponse;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.handlers.ClientDetailsServiceException;
import me.oldboy.mapper.ClientMapper;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.models.client_info.Details;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.GenericFilterBean;

import java.util.Optional;

import static me.oldboy.constants.SecurityConstants.JWT_HEADER;
import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ClientController.class,
                                 ClientDetailsServiceException.class
})
class ClientControllerTest {

    @Autowired
    private ClientDetailsServiceException clientDetailsServiceException;

    @MockitoBean
    private ClientService clientService;
    @Mock
    private HttpServletResponse mockHttpServletResponse;
    @InjectMocks
    private ClientController clientController;

    private MockMvc mockMvc;
    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;
    private Client testClient;
    private Details testDetails;
    private ClientAuthRequest testClientAuthRequest;
    private ClientAuthResponse testClientAuthResponse;
    private Long testId;
    private String testJwt;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        /*
            Нам нужно добавить обработчик ошибок и самое важное - "подменить" MockHttpServletResponse,
            который предоставляет MockMvc. Фактически это не подмена, мы просто задаем тот "ответ",
            который ожидаем получить, в случае корректной работы фильтров.
        */
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setControllerAdvice(clientDetailsServiceException)
                .addFilter(new GenericFilterBean() {
                    @Override
                    @SneakyThrows
                    public void doFilter(ServletRequest request,
                                         ServletResponse response,
                                         FilterChain chain) {
                        ((MockHttpServletResponse) response).setHeader(JWT_HEADER, testJwt);    // Задаем заголовок с токеном
                        chain.doFilter(request, response);  // Вызываем цепь фильтров
                    }
                }, "/api/loginClient")  // Задаем URL на котором вызываем, для универсальности можно и "/*"
                .build();

        objectMapper = new ObjectMapper();

        testId = 1L;

        testJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImF1dGhvcml0aWVzIjoiTU" +
                "9SRSBCQUQgQUNUSU9OLEFETUlOIn0.LImgJxRtwjdDFPdy5od6W5AgeDV7o7t-gAq-vFwqGwY";

        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = ClientCreateDto.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASS)
                .details(testDetailsCreateDto)
                .build();

        testDetails = Details.builder()
                .id(testId)
                .age(TEST_AGE)
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .build();
        testClient = Client.builder()
                .id(testId)
                .role(Role.USER)
                .details(testDetails)
                .email(TEST_EMAIL)
                .pass(TEST_PASS)
                .build();
        testClientAuthRequest = new ClientAuthRequest(TEST_EMAIL, TEST_PASS);
        testClientAuthResponse = new ClientAuthResponse(testId, TEST_EMAIL, testJwt);
    }

    /* В этих тестах мы не будем тестировать валидацию, сделаем это в IT тестах, тут проверяем логику и возврат */

    @Test
    @SneakyThrows
    void shouldReturnClientReadDto_RegistrationClient_Test() {
        ClientReadDto testClientReadDto = ClientMapper.INSTANCE.mapToClientReadDto(testClient);

        String validDto = objectMapper.writeValueAsString(testClientCreateDto);
        String readDto = objectMapper.writeValueAsString(testClientReadDto);

        when(clientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.empty());

        /*
        Тут есть тонкий момент - метод *..saveClient() внутри себя проводит разные обработки с входными данными, т.е.
        то что попало на вход будет изменено сильно и до выхода доберется совсем не то, что "можно ждать - thenReturn",
        поэтому вместо конкретики на вход метода мы подаем - any(ClientCreateDto.class)), но зато на выходе получим
        ожидаемое.
        */
        when(clientService.saveClient(any(ClientCreateDto.class))).thenReturn(testClientReadDto);

        mockMvc.perform(post("/api/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(content().string(readDto));

        verify(clientService, times(1)).findByEmail(anyString());
        verify(clientService, times(1)).saveClient(any(ClientCreateDto.class));
    }

    @Test
    @SneakyThrows
    void shouldReturnException_DuplicateRegEmail_RegistrationClient_Test() {
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);

        when(clientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.of(testClient));

        mockMvc.perform(post("/api/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Email: " + testClientCreateDto.getEmail() + " is exist, can not duplicate data.")));

        verify(clientService, times(1)).findByEmail(anyString());
    }

    /* Самые важные настройки данного теста прошли в методе setUp(), при конфигурировании mockMvc - имитация работы фильтра */
    @Test
    @SneakyThrows
    void shouldReturnOk_AndExpectedAuthResponse_LoginClient_Test() {
        String validAuthRequest = objectMapper.writeValueAsString(testClientAuthRequest);
        String expectedAuthResponse = objectMapper.writeValueAsString(testClientAuthResponse);

        when(clientService.getClientIfAuthDataCorrect(any(ClientAuthRequest.class))).thenReturn(Optional.of(testClient));

        mockMvc.perform(post("/api/loginClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAuthRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedAuthResponse));
    }
}