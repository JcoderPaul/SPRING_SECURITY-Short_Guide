package me.oldboy.unit.controllers.webui;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.webui.LoginRegController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.repository.ClientRepository;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginRegController.class)
class LoginRegControllerTest {

    @MockBean
    private ClientService mockClientService;
    @MockBean
    private DataSource mockDataSource;
    @MockBean
    private ClientRepository mockClientRepository;
    @MockBean
    private ClientDetailsService mockClientDetailsService;
    @Autowired
    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<ClientCreateDto> clientCaptor;

    private Client testClient;
    private Details testDetails;
    private UserDetails testUserDetails;
    private ClientCreateDto testClientCreateDto, noValidClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto, noValidDetailsCreateDto;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setStaticContent(){
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setTestData(){
        testClient = Client.builder()
                .id(1L)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.ROLE_USER)
                .details(testDetails)
                .build();

        testDetails = Details.builder()
                .id(1L)
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .client(testClient)
                .build();

        testUserDetails = new SecurityClientDetails(testClient);

        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = ClientCreateDto.builder()
                .email(TEST_EMAIL)
                .pass(TEST_PASS)
                .details(testDetailsCreateDto).build();

        noValidClientCreateDto = ClientCreateDto.builder()
                .email("we")
                .pass("1")
                .details(testDetailsCreateDto).build();
        noValidDetailsCreateDto = new DetailsCreateDto("w", "t", -12);
    }

    /* Тестируем GET запросы */

    /* Тест первичной аутентификации */
    @Test
    @SneakyThrows
    void shouldReturnLoginPage_clientLoginPageMethod_Test() {
        mockMvc.perform(get("/webui/login")
                        .with(csrf())) // У нас задействована внешняя форма и активна CSRF защита
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("client_forms/login.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Login</title>")));
    }

    /* Тест обращения к login endpoint-у уже залогиненного пользователя */
    @Test
    @SneakyThrows
    void shouldReturnHelloPage_IfUserDetailsNotNull_clientLoginPageMethod_Test() {
        mockMvc.perform(get("/webui/login")
                        .with(user(testUserDetails))
                        .with(csrf())) // У нас задействована внешняя форма и активна CSRF защита
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/hello"))
                .andExpect(content().string(""));
    }

    /* Тест приветственной страницы для уже залогиненного пользователя */
    @Test
    @SneakyThrows
    public void shouldReturnHelloPage_GetHelloPage_Test() {
        mockMvc.perform(get("/webui/hello")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("/hello.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", hasProperty("username", is(EXIST_EMAIL))));
    }

    /* Мы в данном тесте ничего не передали "на вход", просто обратились к методу, поэтому объекты в атрибутах есть, но их поля null */
    @Test
    @SneakyThrows
    void shouldReturnRegPage_RegClientPage_Test() {
        mockMvc.perform(get("/webui/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("client_forms/registration.html"))
                .andExpect(model().attributeExists("client"))
                .andExpect(model().attributeExists("details"))
                .andExpect(model().attribute("client", instanceOf(ClientCreateDto.class)))
                .andExpect(model().attribute("details", instanceOf(DetailsCreateDto.class)))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Registration</title>")));;
    }

    /* Тестируем POST запросы */

    /* Тест нормальной регистрации, с валидными данными */
    @Test
    @SneakyThrows
    void shouldRedirectToLoginPage_WithValidData_AndSuccessReg_CreateClientWebUi_Test() {
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto)
                        .with(csrf())) // Мы используем CSRF защиту
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(mockClientService,times(1)).saveClient(ArgumentMatchers.any(ClientCreateDto.class));
    }

    /* Тестируем логику сохранения данных - сличаем, что прилетело на вход и что получили на выходе */
    @Test
    void createClientWebUi_ValidInput_SavesClientWithDetails_Test() throws Exception {
        clientCaptor = ArgumentCaptor.forClass(ClientCreateDto.class);

        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(mockClientService).saveClient(clientCaptor.capture());
        ClientCreateDto savedClient = clientCaptor.getValue();

        assertAll(
                () -> assertThat(savedClient.getEmail()).isEqualTo(testClientCreateDto.getEmail()),
                () -> assertThat(savedClient.getPass()).isEqualTo(testClientCreateDto.getPass()),
                () -> assertThat(savedClient.getDetails()).isNotNull(),
                () -> assertThat(savedClient.getDetails().clientName()).isEqualTo(testDetailsCreateDto.clientName()),
                () -> assertThat(savedClient.getDetails().clientSurName()).isEqualTo(testDetailsCreateDto.clientSurName()),
                () -> assertThat(savedClient.getDetails().age()).isEqualTo(testDetailsCreateDto.age())
        );
    }

    /* Тестируем работу валидации */
    @Test
    @SneakyThrows
    void shouldRedirectToRegPage_WithNoValidClientData_CreateClientWebUi_Test() {
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", noValidClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto)
                        .with(csrf())) // Мы используем CSRF защиту
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/registration"))
                .andExpect(flash().attributeExists("client"))
                .andExpect(flash().attributeExists("errorsClient"))
                .andExpect(flash().attributeExists("details"));

        verifyNoInteractions(mockClientService);
    }

    @Test
    @SneakyThrows
    void shouldRedirectToRegPage_WithNoValidDetailsData_CreateClientWebUi_Test() {
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", noValidDetailsCreateDto)
                        .with(csrf())) // Мы используем CSRF защиту
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/registration"))
                .andExpect(flash().attributeExists("client"))
                .andExpect(flash().attributeExists("details"))
                .andExpect(flash().attributeExists("errorsDetails"));

        verifyNoInteractions(mockClientService);
    }
}