package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.api.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.EmailNotFoundException;
import me.oldboy.integration.TestContainerInit;
import me.oldboy.models.Client;
import me.oldboy.models.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@AutoConfigureMockMvc
class ClientControllerIT extends TestContainerInit {

    @Autowired
    private ClientController clientController;
    @Autowired
    private MockMvc mockMvc;

    private ClientCreateDto validClientCreateDto, withExistEmailClientDto, notValidClientDto;
    private DetailsCreateDto validDetailsCreateDto;
    private ClientReadDto testClientReadDto;
    private Client testClient;

    @BeforeEach
    void setUp(){
        validDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        validClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, validDetailsCreateDto);
        testClientReadDto = new ClientReadDto(TEST_EMAIL, Role.ROLE_USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);

        withExistEmailClientDto = new ClientCreateDto(EXIST_EMAIL, TEST_PASS, validDetailsCreateDto);
        notValidClientDto = new ClientCreateDto("we", "3", validDetailsCreateDto);

        testClient = Client.builder()
                .email(TEST_EMAIL)
                .pass(WRONG_PASS)
                .role(Role.ROLE_ADMIN)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /*
    Ниже приведены два варианта тестирования метода *.getAdminName() или endpointa - /api/admin/helloAdmin,
    с применением аннотации @WithMockUser и с применением метода .with() класса MockHttpServletRequestBuilder.
    */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnWelcomeStringWithAuthClient_MockAuthentication_HelloAdminTest() {
        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string("This page for ADMIN only! \nHello: " + EXIST_EMAIL));
    }

    /* Проверяем вывод данных по запросу */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnJsonCollectionOfClients_AndContainsSettingString_GetAllClientTest() {
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"email\":\"admin@test.com\"," +
                        "\"role\":\"ROLE_ADMIN\"," +
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
    @WithUserDetails(value = "user@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnForbidden_NotAdminUser_GetAllClientTest() {
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* Проверяем верную регистрацию */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void successfulRegistration_ShouldReturnRegisteredClientData_RegistrationClientTest() {
        mockMvc.perform(post("/api/admin/regClient")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validClientCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(testClientReadDto)));
    }

    /* Проверяем попытку регистрации уже существующего в БД e-mail-а */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturn_4xx_DuplicateEmail_RegistrationClientTest() {
        mockMvc.perform(post("/api/admin/regClient")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(withExistEmailClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"exceptionMsg\":\"Email: " + EXIST_EMAIL + " is exist.\"}"));
    }

    /* Проверяем валидацию */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturn_4xx_NotValidDto_RegistrationClientTest() {
        mockMvc.perform(post("/api/admin/regClient")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(notValidClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Password size can be between 2 and 64")))
                .andExpect(content().string(containsString("Standard e-mail structure - email_name@email_domain.top_lavel_domain")));
    }
}