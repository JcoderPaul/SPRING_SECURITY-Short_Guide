package me.oldboy.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.handlers.DtoValidationHandler;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.Role;
import me.oldboy.test_config.TestConstantFields;
import me.oldboy.test_config.TestDataSourceConfig;
import me.oldboy.test_config.TestSecurityConfig;
import me.oldboy.test_config.TestWebInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
@ContextConfiguration(classes = {TestDataSourceConfig.class,
                                 TestSecurityConfig.class,
                                 TestWebInitializer.class})
@ExtendWith(SpringExtension.class)
class ClientControllerTestIT {

    @Autowired
    private ClientController clientController;
    @Autowired
    private WebApplicationContext applicationContext;
    @Autowired
    private DtoValidationHandler dtoValidationHandler;
    @Autowired
    private Validator validator;
    private MockMvc mockMvc;

    private ClientCreateDto validClientCreateDto, withExistEmailClientDto, notValidClientDto;
    private DetailsCreateDto validDetailsCreateDto;
    private ClientReadDto testClientReadDto;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setMapper(){
        mapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        validDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME, TestConstantFields.TEST_CLIENT_SUR_NAME, TestConstantFields.TEST_AGE);
        validClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL, TestConstantFields.TEST_PASS, validDetailsCreateDto);
        testClientReadDto = new ClientReadDto(TestConstantFields.TEST_EMAIL, Role.USER.name(), TestConstantFields.TEST_CLIENT_NAME, TestConstantFields.TEST_CLIENT_SUR_NAME, TestConstantFields.TEST_AGE);

        withExistEmailClientDto = new ClientCreateDto(TestConstantFields.EXIST_EMAIL, TestConstantFields.TEST_PASS, validDetailsCreateDto);
        notValidClientDto = new ClientCreateDto("", "3", validDetailsCreateDto);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnWelcomeStringWithAuthClientAdminNameTest() {
        SecurityClientDetails clientDetails = (SecurityClientDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string("This page for ADMIN only! \nHello: " + clientDetails.getClientName()));
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
                        .content(mapper.writeValueAsString(validClientCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(mapper.writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(testClientReadDto)));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com")
    void shouldReturn_4xx_DuplicateEmail_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(withExistEmailClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"exceptionMsg\":\"Email: " + TestConstantFields.EXIST_EMAIL + " is exist.\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "admin@test.com")
    void shouldReturn_4xx_NotValidDto_RegistrationClientTest() {
        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(notValidClientDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Field cannot be blank")))
                .andExpect(content().string(containsString("Field size value can be between 2 and 64")));
    }
}