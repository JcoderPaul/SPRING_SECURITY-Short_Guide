package me.oldboy.integration.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class LoginRegControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientService clientService;
    private ClientCreateDto testClientCreateDto, noValidClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto, noValidDetailsCreateDto;

    @BeforeEach
    void setTestData(){
        /* Валидные данные для создания записи в БД */
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = ClientCreateDto.builder()
                .email(TEST_EMAIL)
                .pass(TEST_PASS)
                .details(testDetailsCreateDto).build();

        /* Не валидные данные - тесты исключений */
        noValidClientCreateDto = ClientCreateDto.builder()
                .email("we")
                .pass("1")
                .details(testDetailsCreateDto).build();
        noValidDetailsCreateDto = new DetailsCreateDto("w", "t", -12);
    }


    @Test
    @SneakyThrows
    void getLoginPage_ShouldReturnOk_Test(CapturedOutput output) {
        mockMvc.perform(get("/webui/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("client_forms/login.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Login</title>")));

        assertThat(output).contains("Redirect to - client_forms/login.html - from @GetMapping('/login')");
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getMainPage_ShouldReturnOk_Test(CapturedOutput output) {
        mockMvc.perform(get("/webui/main"))
                .andExpect(status().isOk())
                .andExpect(view().name("/main.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Main</title>")));

        assertThat(output).contains("Redirect to - /main.html - from @GetMapping(/main)");
    }

    @Test
    @SneakyThrows
    void regClientPage_ShouldReturnOk_Test() {
        mockMvc.perform(get("/webui/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("client_forms/registration.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Registration</title>")));
    }

    @Test
    @SneakyThrows
    void createClientWebUi_ShouldReturnOk_AndCreatedClient_Test() {
        /* Регистрируем нового клиента в тестовую БД */
        mockMvc.perform(post("/webui/registration")
                        .with(csrf())   // Незабываем у нас активна CSRF защита, а тестируем мы POST метод
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
    void createClientWebUi_ShouldRedirectToRegPage_WithNoValidClientData_Test() {
        MvcResult result = mockMvc.perform(post("/webui/registration")
                        .with(csrf())   // Незабываем у нас активна CSRF защита, а тестируем мы POST метод
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
    void createClientWebUi_ShouldRedirectToRegPage_WithNoValidDetailsData_Test() {
        MvcResult mvcResult = mockMvc.perform(post("/webui/registration")
                        .with(csrf())   // Незабываем у нас активна CSRF защита, а тестируем мы POST метод
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