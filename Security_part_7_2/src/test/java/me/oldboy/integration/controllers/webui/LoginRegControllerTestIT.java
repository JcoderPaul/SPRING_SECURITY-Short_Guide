package me.oldboy.integration.controllers.webui;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IT
class LoginRegControllerTestIT extends TestContainerInit {

    @Autowired
    private ClientService clientService;
    @Autowired
    private WebApplicationContext webAppContext;
    private MockMvc mockMvc;

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
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(springSecurity())
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
    @Test
    @SneakyThrows
    void shouldReturnLoginPage_clientLoginPageMethod_Test() {
        mockMvc.perform(get("/webui/login")
                        .with(csrf())) // У нас задействована внешняя форма и активна CSRF защита
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("templates/client_forms/login.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    @SneakyThrows
    public void shouldReturnHelloPage_GetHelloPage_Test() {
        mockMvc.perform(get("/webui/hello")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("templates/hello.html"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", hasProperty("username", is(EXIST_EMAIL))))
                .andExpect(content().string(containsString("HELLO FROM MODEL ATTRIBUTE:")))
                .andExpect(content().string(containsString("HELLO FROM SPRING SECURITY CONTEXT:")))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
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
                .andExpect(model().attribute("details", instanceOf(DetailsCreateDto.class)))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    /* Тестируем POST запросы */
    @Test
    @SneakyThrows
    void shouldRedirectToLoginPage_WithValidData_AndSuccessReg_CreateClientWebUi_Test() {
        /* Регистрируем нового клиента в тестовую БД */
        mockMvc.perform(post("/webui/registration")
                        .flashAttr("client", testClientCreateDto)
                        .flashAttr("details", testDetailsCreateDto)
                        .with(csrf())) // Мы используем CSRF защиту
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
                        .flashAttr("details", testDetailsCreateDto)
                        .with(csrf())) // Мы используем CSRF защиту
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
                        .flashAttr("details", noValidDetailsCreateDto)
                        .with(csrf())) // Мы используем CSRF защиту
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