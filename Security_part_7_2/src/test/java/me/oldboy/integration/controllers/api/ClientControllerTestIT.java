package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class ClientControllerTestIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext webAppContext;

    private MockMvc mockMvc;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto, testExistEmailClientCreateDto;
    private ClientReadDto testClientReadDto;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setStaticContent(){
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(springSecurity())
                .build();

        testClientReadDto = new ClientReadDto(TEST_EMAIL, Role.ROLE_USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
        testExistEmailClientCreateDto = new ClientCreateDto(EXIST_EMAIL, TEST_PASS, testDetailsCreateDto);
        notValidCreateClientDto = new ClientCreateDto("sd","1", testDetailsCreateDto);
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, roles = TEST_STR_ROLE_ADMIN)
    void shouldReturnWelcomeString_AdminWithAuthUser_HelloAdminTest() { // Попытка перейти на страницу приветствия ADMIN-ом - ок
        String expected = "This page for ADMIN only! \nHello: " + EXIST_EMAIL;

        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = TEST_EMAIL, password = TEST_PASS)
    void shouldReturn_4xx_Forbidden_NoAdminWithAuthUser_HelloAdminTest() {    // Попытка перейти на стр. приветствия не ADMIN-ом - forbidden
        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, roles = TEST_STR_ROLE_ADMIN)
    void shouldReturn_OkAndTestedList_GetAllClientTest() {  // Попытка получить данные ADMIN-ом - ок
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"email\":\"admin@test.com\"," +
                                                            "\"role\":\"ROLE_ADMIN\"," +
                                                            "\"clientName\":\"Malkolm\"," +
                                                            "\"clientSurName\":\"Stone\"," +
                                                            "\"age\":19}")));;
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS)
    void shouldReturn_4xx_WithOutAdminAuth_GetAllClientTest() {  // Попытка получить данные не ADMIN-ом - forbidden
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }

    /*
        Тестируем endpoint "регистрации", доступ к нему имеет только ADMIN. У нас есть два варианта передать
        данные аутентификации, часто применяемый в подобных случаях, аннотация см. выше:
        @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, roles = TEST_STR_ROLE_ADMIN)

        Второй вариант, передать данные аутентификации в запросе используя, например, конструкцию см. ниже:
        with(httpBasic(EXIST_EMAIL, TEST_PASS))

        Поскольку у нас в данном случае интеграционный тест, то такой запрос позволит поднять из тестовой БД
        данные на аутентифицированного пользователя, получить его ROLE и Authority, и уточнить его статус или
        "дозволения" на совершение текущей операции исходя из настоек FilterChain.
    */

    @Test
    @SneakyThrows
    void shouldReturn_4xx_DuplicateEmail_RegistrationClientTest() { // Попытка регистрации с существующим в БД email
        mockMvc.perform(post("/api/admin/regClient")
                        .with(httpBasic(EXIST_EMAIL, TEST_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testExistEmailClientCreateDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"exceptionMsg\":\"Email: " + EXIST_EMAIL + " is exist.\"}"));
    }

    @Test
    @SneakyThrows
    void shouldReturn_ValidationErrors_RegistrationClientTest() {   // Попытка регистрации невалидных данных - validation error
        String notValidDto = objectMapper.writeValueAsString(notValidCreateClientDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .with(httpBasic(EXIST_EMAIL, TEST_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidDto))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Password size can be between 2 and 64")))
                .andExpect(content().string(containsString("Standard e-mail structure - email_name@email_domain.top_lavel_domain")));;
    }

    @Test
    @SneakyThrows
    void shouldReturn_ValidationOk_RegistrationClientTest() {   // Попытка регистрации валидных данных - ок
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);
        String readDto = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(testClientReadDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .with(httpBasic(EXIST_EMAIL, TEST_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(content().string(readDto));
    }
}