package me.oldboy.integration.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static me.oldboy.constants.SecurityConstants.JWT_HEADER;
import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class LoginRegControllerTestIT extends IntegrationTestBase {

    @Autowired
    private ClientService clientService;
    @MockBean
    private AuthenticationEventListener mockAuthenticationEventListener;
    @Autowired
    private MockMvc mockMvc;

    private Client testClient;
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

    @Test
    @SneakyThrows
    void shouldReturnLoginPage_ClientLoginPage_Test(CapturedOutput output) {
        mockMvc.perform(get("/webui/login"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("client_forms/login.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Login</title>")));

        assertThat(output).contains("Redirect to - client_forms/login.html - from @GetMapping(login)");
    }

    @Test
    @SneakyThrows
    void shouldReturnRedirectToJwtTokenPage_ClientLoginPage_Test(CapturedOutput output) {
        when(mockAuthenticationEventListener.getAuthenticationAfterFormLogin())
                .thenReturn(new TestingAuthenticationToken(testClient, TEST_PASS, testUserDetails.getAuthorities()));

        mockMvc.perform(get("/webui/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/jwt_token"));

        assertThat(output).contains("Redirect to - /webui/jwt_token - from @GetMapping(login)");
    }

    @Test
    @SneakyThrows
    public void shouldReturn_ContinuePage_WithJwtToken_GetJwtAndContinue_Test(CapturedOutput output) {
        mockMvc.perform(get("/webui/jwt_token")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("/continue.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Continue</title>")));

        assertThat(output).contains("Redirect to - /continue.html - from @GetMapping(/jwt_token)");
    }

    @Test
    @SneakyThrows
    public void shouldRedirect_ToLoginPage_WithoutJwtToken_GetJwtAndContinue_Test() {
        mockMvc.perform(get("/webui/jwt_token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/login"));
    }

    @Test
    @SneakyThrows
    void shouldReturnMainPage_PostMainPage_Test(CapturedOutput output) {
        mockMvc.perform(post("/webui/main")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("/main.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Main</title>")));

        assertThat(output).contains("Redirect to - /main.html - from @PostMapping(/main)");
    }

    @Test
    @SneakyThrows
    public void shouldRedirect_ToLoginPage_WithoutJwtToken_PostMainPage_Test() {
        mockMvc.perform(post("/webui/main"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    void shouldReturnMainPage_GetMainPage_Test(CapturedOutput output) {
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
    public void shouldRedirect_ToLoginPage_WithoutJwtToken_GetMainPage_Test() {
        mockMvc.perform(get("/webui/main"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    void shouldRedirectToLoginPage_WithValidData_AndSuccessReg_CreateClientWebUi_Test() {
        /* Регистрируем нового клиента в тестовую БД */
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        /* Извлекаем клиента из тестовой БД */
        Optional<Client> mayBeClient = clientService.findByEmail(testClientCreateDto.getEmail());

        /* Сверяем исходные данные с полученными из БД после регистрации */
        if (mayBeClient.isPresent()) {
            Client clientFromBase = mayBeClient.get();
            assertAll(
                    () -> assertThat(clientFromBase.getEmail()).isEqualTo(testClientCreateDto.getEmail()),
                    () -> assertThat(clientFromBase.getDetails().getClientName()).isEqualTo(testClientCreateDto.getDetails().clientName()),
                    () -> assertThat(clientFromBase.getDetails().getClientSurName()).isEqualTo(testClientCreateDto.getDetails().clientSurName()),
                    () -> assertThat(clientFromBase.getDetails().getAge()).isEqualTo(testClientCreateDto.getDetails().age())
            );
        }
    }

    /* Тестируем работу валидации */
    @Test
    @SneakyThrows
    void shouldRedirectToRegPage_WithNoValidClientData_CreateClientWebUi_Test() {
        MvcResult result = mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", noValidClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/registration"))
                .andExpect(flash().attributeExists("client"))
                .andExpect(flash().attributeExists("errorsClient"))
                .andExpect(flash().attributeExists("details"))
                .andReturn();

        String strRes = result.getFlashMap().get("errorsClient").toString();
        assertThat(strRes.contains("Password size can be between 2 and 64")).isTrue();
        assertThat(strRes.contains("Standard e-mail structure - email_name@email_domain.top_lavel_domain (for example: paul@tradsystem.ru)")).isTrue();
    }

    @Test
    @SneakyThrows
    void shouldRedirectToRegPage_WithNoValidDetailsData_CreateClientWebUi_Test() {
        MvcResult mvcResult = mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", noValidDetailsCreateDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/registration"))
                .andExpect(flash().attributeExists("client"))
                .andExpect(flash().attributeExists("details"))
                .andExpect(flash().attributeExists("errorsDetails"))
                .andReturn();

        String strRes = mvcResult.getFlashMap().get("errorsDetails").toString();
        assertThat(strRes.contains("Age can't be lass then 0, unless you come from a counter-directional universe!")).isTrue();
        assertThat(strRes.contains("Name field size can be between 2 and 64")).isTrue();
        assertThat(strRes.contains("Surname field size can be between 2 and 64")).isTrue();
    }
}