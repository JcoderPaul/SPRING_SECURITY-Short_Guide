package me.oldboy.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.Role;
import me.oldboy.test_content.TestConstantFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
@AutoConfigureMockMvc
class ClientControllerTestIT {

    @Autowired
    private ClientController clientController;
    @Autowired
    private MockMvc mockMvc;

    private ClientCreateDto validClientCreateDto, withExistEmailClientDto, notValidClientDto;
    private DetailsCreateDto validDetailsCreateDto;
    private ClientReadDto testClientReadDto;

    @BeforeEach
    void setUp(){
        validDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME,
                                                     TestConstantFields.TEST_CLIENT_SUR_NAME,
                                                     TestConstantFields.TEST_AGE);
        validClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL,
                                                   TestConstantFields.TEST_PASS,
                                                   validDetailsCreateDto);
        testClientReadDto = new ClientReadDto(TestConstantFields.TEST_EMAIL,
                                              Role.USER.name(),
                                              TestConstantFields.TEST_CLIENT_NAME,
                                              TestConstantFields.TEST_CLIENT_SUR_NAME,
                                              TestConstantFields.TEST_AGE);

        withExistEmailClientDto = new ClientCreateDto(TestConstantFields.EXIST_EMAIL, TestConstantFields.TEST_PASS, validDetailsCreateDto);
        notValidClientDto = new ClientCreateDto("", "", validDetailsCreateDto);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @SneakyThrows
    void shouldReturnWelcomeStringWithAuthClientAdminNameTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/helloAdmin")
                        .with(httpBasic(TestConstantFields.EXIST_EMAIL, TestConstantFields.TEST_PASS)))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("This page for ADMIN only! \nHello: " + TestConstantFields.EXIST_EMAIL));
    }

    @Test
    @SneakyThrows
    void shouldReturnJsonCollectionOfClients_AndContainsSettingString_GetAllClientTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/getAllClient")
                        .with(httpBasic(TestConstantFields.EXIST_EMAIL, TestConstantFields.TEST_PASS)))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("{\"email\":\"admin@test.com\",\"role\":\"ADMIN\",\"clientName\":\"Malkolm\",\"clientSurName\":\"Stone\",\"age\":19}")));
    }

    @Test
    @SneakyThrows
    void shouldReturn_UnauthorizedClientWithWrongName_GetAllClientTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/getAllClient")
                        .with(httpBasic(TestConstantFields.NON_EXIST_EMAIL, TestConstantFields.TEST_PASS)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void shouldReturn_UnauthorizedClientWithWrongPass_GetAllClientTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/getAllClient")
                        .with(httpBasic(TestConstantFields.EXIST_EMAIL, TestConstantFields.WRONG_PASS)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void successfulRegistration_ShouldReturnRegisteredClientData_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .with(httpBasic(TestConstantFields.EXIST_EMAIL,
                                        TestConstantFields.TEST_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validClientCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(testClientReadDto)));
    }

    @Test
    @SneakyThrows
    void shouldReturn_4xx_DuplicateEmail_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .with(httpBasic(TestConstantFields.EXIST_EMAIL,
                                        TestConstantFields.TEST_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(withExistEmailClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"exceptionMsg\":\"Email: " + TestConstantFields.EXIST_EMAIL + " is exist.\"}"));
    }

    @Test
    @SneakyThrows
    void shouldReturn_4xx_NotValidDto_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .with(httpBasic(TestConstantFields.EXIST_EMAIL,
                                        TestConstantFields.TEST_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(notValidClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Field cannot be blank")))
                .andExpect(content().string(containsString("Field size value can be between 2 and 64")));
    }
}