package me.oldboy.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.Role;
import me.oldboy.test_content.TestFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
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
        validDetailsCreateDto = new DetailsCreateDto(TestFields.TEST_CLIENT_NAME, TestFields.TEST_CLIENT_SUR_NAME, TestFields.TEST_AGE);
        validClientCreateDto = new ClientCreateDto(TestFields.TEST_EMAIL, TestFields.TEST_PASS, validDetailsCreateDto);
        testClientReadDto = new ClientReadDto(TestFields.TEST_EMAIL, Role.USER.name(), TestFields.TEST_CLIENT_NAME, TestFields.TEST_CLIENT_SUR_NAME, TestFields.TEST_AGE);

        withExistEmailClientDto = new ClientCreateDto(TestFields.EXIST_EMAIL, TestFields.TEST_PASS, validDetailsCreateDto);
        notValidClientDto = new ClientCreateDto("", "", validDetailsCreateDto);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com")
    void shouldReturnWelcomeStringWithAuthClientAdminNameTest() {
        SecurityClientDetails clientDetails = (SecurityClientDetails) SecurityContextHolder.getContext()
                                                                                           .getAuthentication()
                                                                                           .getPrincipal();

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("This page for ADMIN only! \nHello: " + clientDetails.getClientName()));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com")
    void shouldReturnJsonCollectionOfClients_AndContainsSettingString_GetAllClientTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("{\"email\":\"admin@test.com\",\"role\":\"ADMIN\",\"clientName\":\"Malkolm\",\"clientSurName\":\"Stone\",\"age\":19}")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "user@test.com")
    void shouldReturn_ForbiddenClientWithOutAdminRole_GetAllClientTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/getAllClient"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com")
    void successfulRegistration_ShouldReturnRegisteredClientData_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validClientCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writer()
                                                              .withDefaultPrettyPrinter()
                                                              .writeValueAsString(testClientReadDto)));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com")
    void shouldReturn_4xx_DuplicateEmail_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(withExistEmailClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"exceptionMsg\":\"Email: " + TestFields.EXIST_EMAIL + " is exist.\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com")
    void shouldReturn_4xx_NotValidDto_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(notValidClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Field cannot be blank")))
                .andExpect(content().string(containsString("Field size value can be between 2 and 64")));
    }
}