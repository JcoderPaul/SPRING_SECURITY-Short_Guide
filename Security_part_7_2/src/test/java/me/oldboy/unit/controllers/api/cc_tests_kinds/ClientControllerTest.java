package me.oldboy.unit.controllers.api.cc_tests_kinds;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.security_config.AppSecurityConfig;
import me.oldboy.controllers.api.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppSecurityConfig.class})
@WebAppConfiguration
@WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, roles = TEST_STR_ROLE_ADMIN)
class ClientControllerTest {

    /* Для тестирования методов getAllClient() и  registrationClient()*/
    @MockitoBean
    private ClientService clientService;

    @Autowired
    private WebApplicationContext webAppContext;

    private MockMvc mockMvc;

    private List<ClientReadDto> testDtoList;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto;
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
        notValidCreateClientDto = new ClientCreateDto("sd","1", testDetailsCreateDto);

        testDtoList = new ArrayList<>(List.of(testClientReadDto, new ClientReadDto(), new ClientReadDto()));
    }

    @Test
    @SneakyThrows
    void shouldReturnWelcomeStringAdminWithAuthUserNameTest() {
        String expected = "This page for ADMIN only! \nHello: " + EXIST_EMAIL;

        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @SneakyThrows
    void shouldReturnOkAndTestedListGetAllClientTest(){
        when(clientService.findAll()).thenReturn(testDtoList);
        /* Выводить полный список смысла нет, возьмем только наглядную часть */
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("{\"email\":\"for_test@test.com\",\"role\":\"ROLE_USER\"," +
                                "\"clientName\":\"Testino\",\"clientSurName\":\"Testorelly\"," +
                                "\"age\":32}")));
    }

    @Test
    void shouldReturn_ValidationErrors_RegistrationClientTest() throws Exception {
        String notValidDto = objectMapper.writeValueAsString(notValidCreateClientDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidDto))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn_ValidationOk_RegistrationClientTest() throws Exception {
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);
        String readDto = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(testClientReadDto);

        when(clientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.empty());
        when(clientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(content().string(readDto));

        verify(clientService, times(1)).findByEmail(anyString());
        verify(clientService, times(1)).saveClient(any(ClientCreateDto.class));
    }
}