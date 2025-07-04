package me.oldboy.unit.controllers.webui;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.webui.LoginRegController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class LoginRegControllerTest {

    @Mock
    private ClientService mockClientService;
    @InjectMocks
    private LoginRegController loginRegController;

    @Autowired
    private WebApplicationContext webAppContext;
    private MockMvc mockMvc;

    private SecurityClientDetails clientDetails;

    @Captor
    private ArgumentCaptor<ClientCreateDto> clientCaptor;

    private Client testClient;
    private Details testDetails;
    private ClientCreateDto testClientCreateDto, noValidClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto, noValidDetailsCreateDto;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setStaticContent(){
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setTestData(){
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(loginRegController)
                .build();

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

       clientDetails = new SecurityClientDetails(testClient);

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
    @Test
    @SneakyThrows
    void shouldReturnLoginPage_clientLoginPageMethod_Test() {
        mockMvc.perform(get("/webui/login")
                        .with(csrf())) // У нас задействована внешняя форма и активна CSRF защита
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("templates/client_forms/login.html"));
    }

    @Test
    @SneakyThrows
    public void shouldReturnHelloPage_GetHelloPage_Test() {
        mockMvc.perform(get("/webui/hello")
                        .principal(new TestingAuthenticationToken(clientDetails, clientDetails.getPassword())))
                .andExpect(status().isOk())
                .andExpect(view().name("templates/hello.html"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", instanceOf(UserDetails.class)));
    }

    @Test
    @SneakyThrows
    void shouldReturnRegPage_RegClientPage_Test() {
        mockMvc.perform(get("/webui/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("templates/client_forms/registration.html"))
                .andExpect(model().attributeExists("client"))
                .andExpect(model().attributeExists("details"))
                .andExpect(model().attribute("client", instanceOf(ClientCreateDto.class)))
                .andExpect(model().attribute("details", instanceOf(DetailsCreateDto.class)));
    }

    /* Тестируем POST запросы */
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

    /* Тестируем логику сохранения данных */
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