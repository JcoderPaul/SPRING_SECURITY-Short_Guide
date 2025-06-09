package me.oldboy.unit.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.webui.LoginRegController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.models.client_info.Details;
import me.oldboy.repository.*;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static me.oldboy.constants.SecurityConstants.JWT_HEADER;
import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginRegController.class)
@ExtendWith(OutputCaptureExtension.class)
class LoginRegControllerTest {

    @MockBean
    private ClientService mockClientService;
    @MockBean
    private DataSource mockDataSource;
    @MockBean
    private ClientRepository mockClientRepository;
    @MockBean
    private ClientDetailsService mockClientDetailsService;
    @MockBean
    private AuthenticationEventListener mockAuthenticationEventListener;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactRepository contactRepository;
    @MockBean
    private BalanceRepository balanceRepository;
    @MockBean
    private CardRepository cardRepository;
    @MockBean
    private LoanRepository loanRepository;

    @Captor
    private ArgumentCaptor<ClientCreateDto> clientCaptor;

    private Client testClient;
    private Details testDetails;
    private UserDetails testUserDetails;
    private ClientCreateDto testClientCreateDto, noValidClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto, noValidDetailsCreateDto;
    private String testJwt;

    @BeforeEach
    void setTestData(){
        testJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImF1dGhvcml0aWVzIjoiTU" +
                "9SRSBCQUQgQUNUSU9OLEFETUlOIn0.LImgJxRtwjdDFPdy5od6W5AgeDV7o7t-gAq-vFwqGwY";

        testClient = Client.builder()
                .id(1L)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
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
                .details(testDetailsCreateDto)
                .build();

        noValidClientCreateDto = ClientCreateDto.builder()
                .email("we")
                .pass("1")
                .details(testDetailsCreateDto)
                .build();
        noValidDetailsCreateDto = new DetailsCreateDto("w", "t", -12);
    }

    /* Тест первичной аутентификации */
    @Test
    @SneakyThrows
    void shouldReturn_2xx_NotAuth_ClientLoginPageMethod_Test(CapturedOutput output) {
        mockMvc.perform(get("/webui/login"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("client_forms/login.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Login</title>")));

        assertThat(output).contains("Redirect to - client_forms/login.html - from @GetMapping(login)");

        /* Метод вызывается 2-а раза, сначала в JwtTokenGeneratorAndAfterFilter цепи безопасности, затем в самом тестируемом методе */
        verify(mockAuthenticationEventListener,times(2)).getAuthenticationAfterFormLogin();
    }

    /* Тест обращения к login endpoint-у уже залогиненного пользователя */
    @Test
    @SneakyThrows
    void shouldReturnJwtPage_IfUserDetailsNotNull_clientLoginPageMethod_Test(CapturedOutput output) {
        when(mockAuthenticationEventListener.getAuthenticationAfterFormLogin())
                .thenReturn(new UsernamePasswordAuthenticationToken(testClient, TEST_PASS, testUserDetails.getAuthorities()));

        mockMvc.perform(get("/webui/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/jwt_token"))
                .andExpect(content().string(""));

        assertThat(output).contains("Redirect to - /webui/jwt_token - from @GetMapping(login)");

        verify(mockAuthenticationEventListener,times(3)).getAuthenticationAfterFormLogin();
    }

    /* Тест обращения к странице с JWT token не залогиненного пользователя - header "Authorization" отсутствует в response */
    @Test
    @SneakyThrows
    public void shouldRedirectToContinuePage_WithAuthUser_GetJwtAndContinue_Test() {
        mockMvc.perform(get("/webui/jwt_token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/login"))
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    public void shouldRedirectToLoginPage_WithoutAuthUser_GetJwtAndContinue_Test(CapturedOutput output) {
        mockMvc.perform(get("/webui/jwt_token")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("/continue.html"));

        assertThat(output).contains("Redirect to - /continue.html - from @GetMapping(/jwt_token)");
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
                .andExpect(content().string(containsString("<title>Registration</title>")));
    }

    @Test
    @SneakyThrows
    public void shouldRedirectToMainPage_WithAuthUser_GetMainPage_Test(CapturedOutput output) {
        mockMvc.perform(get("/webui/main")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("/main.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Main</title>")));

        assertThat(output).contains("Redirect to - /main.html - from @GetMapping(/main)");
    }

    @Test
    @SneakyThrows
    public void shouldRedirectToMainPage_WithAuthUser_postMainPage_Test(CapturedOutput output) {
        mockMvc.perform(post("/webui/main")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("/main.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Main</title>")));

        assertThat(output).contains("Redirect to - /main.html - from @PostMapping(/main)");
        assertThat(output).contains("Response header from @PostMapping(/main): ");
    }

    /* Тест нормальной регистрации, с валидными данными */
    @Test
    @SneakyThrows
    void shouldRedirectToLoginPage_WithValidData_AndSuccessReg_CreateClientWebUi_Test() {
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto))
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
                        .flashAttr("details", testDetailsCreateDto))
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
    void shouldRedirectToRegPage_WithNoValid_ClientData_CreateClientWebUi_Test() {
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", noValidClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/registration"))
                .andExpect(flash().attributeExists("client"))
                .andExpect(flash().attributeExists("errorsClient"))
                .andExpect(flash().attributeExists("details"));

        verifyNoInteractions(mockClientService);
    }

    @Test
    @SneakyThrows
    void shouldRedirectToRegPage_WithNoValid_DetailsData_CreateClientWebUi_Test() {
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", noValidDetailsCreateDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/registration"))
                .andExpect(flash().attributeExists("client"))
                .andExpect(flash().attributeExists("details"))
                .andExpect(flash().attributeExists("errorsDetails"));

        verifyNoInteractions(mockClientService);
    }
}