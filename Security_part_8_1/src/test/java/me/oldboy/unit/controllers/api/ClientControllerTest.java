package me.oldboy.unit.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.api.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.repository.ClientRepository;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, roles = {TEST_STR_ROLE_ADMIN})
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private ClientService clientService;

    private List<ClientReadDto> testDtoList;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto;
    private ClientReadDto testClientReadDto;
    private static ObjectMapper objectMapper;
    private Client testClient;
    private Details testDetails;
    private RememberMeAuthenticationToken rememberMeAuth;
    private SecurityClientDetails securityClientDetails;

    @BeforeAll
    static void setStaticContent(){
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp(){
        testClientReadDto = new ClientReadDto(TEST_EMAIL, Role.ROLE_USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
        notValidCreateClientDto = new ClientCreateDto("sd","1", testDetailsCreateDto);

        testClient = Client.builder()
                .id(1L)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.ROLE_ADMIN)
                .details(testDetails)
                .build();

        testDetails = Details.builder()
                .id(1L)
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .client(testClient)
                .build();

        securityClientDetails =  new SecurityClientDetails(testClient);
        rememberMeAuth = new RememberMeAuthenticationToken("testKey", securityClientDetails, securityClientDetails.getAuthorities());
        testDtoList = new ArrayList<>(List.of(testClientReadDto, new ClientReadDto(), new ClientReadDto()));
    }

    @Test
    @SneakyThrows
    void shouldReturnWelcomeString_AdminWithAuth_UserNameTest() {
        String expected = "This page for ADMIN only! \nHello: " + EXIST_EMAIL;

        mockMvc.perform(get("/api/admin/helloAdmin")
                        .principal(rememberMeAuth))
                .andExpect(status().isOk())
                .andExpect(content().string(expected))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    @SneakyThrows
    void shouldReturnOkAndTestedList_GetAllClientTest(){
        when(clientService.findAll()).thenReturn(testDtoList);
        /* Выводить полный список смысла нет, возьмем только наглядную часть */
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("{\"email\":\"for_test@test.com\"," +
                        "\"role\":\"ROLE_USER\"," +
                        "\"clientName\":\"Testino\"," +
                        "\"clientSurName\":\"Testorelly\"," +
                        "\"age\":32}")));
    }

    @Test
    void shouldReturn_ValidationErrors_RegistrationClientTest() throws Exception {
        String notValidDto = objectMapper.writeValueAsString(notValidCreateClientDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidDto))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("Standard e-mail structure - email_name@email_domain.top_lavel_domain")))
                .andExpect(content().string(containsString("Password size can be between 2 and 64")));
    }

    @Test
    void shouldReturn_4xx_DuplicateEmail_RegistrationClientTest() throws Exception {
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);

        when(clientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.of(testClient));

        mockMvc.perform(post("/api/admin/regClient")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"exceptionMsg\":\"Email: for_test@test.com is exist.\"}"));
    }

    /*
    Интересная ситуация именно с этим тестом. Происходит его падение, т.к. на выходе вместо ожидаемого JSON мы получаем null.
    Проблему решает полное "перетряхивание" ClientCreateDto. Т.е. минимизируем количество lombok аннотаций над классом, а геттеры,
    сеттеры и остальную "обвязку" создаем руками. И только тогда тест проходит нормально, хотя при полностью "обломоченом" классе
    ClientCreateDto приложение работало без нареканий и проблем не возникало.
    */
    @Test
    void shouldReturn_ValidationOk_RegistrationClientTest() throws Exception {
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);
        String readDto = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(testClientReadDto);

        when(clientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.empty());
        when(clientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(content().string(readDto));

        verify(clientService, times(1)).findByEmail(anyString());
        verify(clientService, times(1)).saveClient(any(ClientCreateDto.class));
    }
}