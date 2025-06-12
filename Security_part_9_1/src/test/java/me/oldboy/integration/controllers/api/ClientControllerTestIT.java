package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.controllers.api.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ClientControllerTestIT extends IntegrationTestBase {

    @Autowired
    private ClientController clientController;
    @Autowired
    private MockMvc mockMvc;

    private ClientCreateDto validClientCreateDto, withExistEmailClientDto, notValidClientDto;
    private DetailsCreateDto validDetailsCreateDto;
    private ClientReadDto testClientReadDto;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        objectMapper = new ObjectMapper();

        validDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        validClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, validDetailsCreateDto);
        testClientReadDto = new ClientReadDto(TEST_EMAIL, Role.USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);

        withExistEmailClientDto = new ClientCreateDto(EXIST_EMAIL, TEST_PASS, validDetailsCreateDto);
        notValidClientDto = new ClientCreateDto("we", "3", validDetailsCreateDto);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, roles = "ADMIN")
    void shouldReturnWelcomeString_WithAuthClient_HelloAdminTest() {
        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string("This page for ADMIN only! \nHello: " + EXIST_EMAIL));
    }

    @Test
    @SneakyThrows
    void shouldReturn_3xx_WithoutAuthClient_HelloAdminTest() {
        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    /* Проверяем вывод данных по запросу */
    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, roles = "ADMIN")
    void shouldReturnJsonCollectionOfClients_AndContainsSettingString_GetAllClientTest() {
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"email\":\"admin@test.com\"," +
                        "\"role\":\"ADMIN\"," +
                        "\"clientName\":\"Malkolm\"," +
                        "\"clientSurName\":\"Stone\"," +
                        "\"age\":19}")));
    }

    /* Проверяем редирект без аутентификации */
    @Test
    @SneakyThrows
    void shouldReturn_3xx_UnauthorizedClient_GetAllClientTest() {
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    /* Проверяем отказ в доступе при не ADMIN роли */
    @Test
    @SneakyThrows
    @WithMockUser(username = TEST_EMAIL, roles = "USER")
    void shouldReturnForbidden_NotAdminUser_GetAllClientTest() {
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* Проверяем верную регистрацию */
    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, roles = "ADMIN")
    void successfulRegistration_ShouldReturnRegisteredClientData_RegistrationClientTest() {
        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClientCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(testClientReadDto)));
    }

    /* Проверяем попытку регистрации уже существующего в БД e-mail-а */
    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, roles = "ADMIN")
    void shouldReturn_4xx_DuplicateEmail_RegistrationClientTest() {
        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withExistEmailClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"exceptionMsg\":\"Email: " + EXIST_EMAIL + " is exist.\"}"));
    }

    /* Проверяем валидацию */
    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, roles = "ADMIN")
    void shouldReturn_4xx_NotValidDto_RegistrationClientTest() {
        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notValidClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Password size can be between 2 and 64")))
                .andExpect(content().string(containsString("Standard e-mail structure - email_name@email_domain.top_lavel_domain")));
    }
}