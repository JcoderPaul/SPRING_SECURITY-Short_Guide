package me.oldboy.unit.controllers.cc_test_config_mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* Задействуем в тесте имитацию слоя контроллеров, без полного поднятия контекста - 7 сек. */
@WebMvcTest(ClientController.class)
@WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, authorities = {TEST_STR_ROLE_ADMIN})
class ClientControllerWebMvcMockTest {

    @MockBean
    private ClientService mockClientService;
    @MockBean
    private DataSource mockDataSource;
    @MockBean
    private ClientRepository mockClientRepository;
    @Autowired
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
        testClientReadDto = new ClientReadDto(TEST_EMAIL, Role.USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
        notValidCreateClientDto = new ClientCreateDto("sd","1", testDetailsCreateDto);

        testDtoList = new ArrayList<>(List.of(testClientReadDto, new ClientReadDto(), new ClientReadDto()));
    }

    @Test
    @SneakyThrows
    void shouldReturnWelcomeStringAdminWithAuthUserNameTest() {
        String expected = "This page for ADMIN only! \nHello: " + EXIST_EMAIL;

        mockMvc.perform(get("/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @SneakyThrows
    void shouldReturnOkAndTestedListGetAllClientTest(){
        when(mockClientService.findAll()).thenReturn(testDtoList);
        /* Выводить полный список смысла нет, возьмем только наглядную часть */
        mockMvc.perform(get("/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("{\"email\":\"for_test@test.com\"," +
                                                        "\"role\":\"USER\"," +
                                                        "\"clientName\":\"Testino\"," +
                                                        "\"clientSurName\":\"Testorelly\"," +
                                                        "\"age\":32}")));
    }

    @Test
    void shouldReturn_ValidationErrors_RegistrationClientTest() throws Exception {
        String notValidDto = objectMapper.writeValueAsString(notValidCreateClientDto);

        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidDto))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn_ValidationOk_RegistrationClientTest() throws Exception {
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);
        String readDto = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(testClientReadDto);

        when(mockClientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.empty());
        when(mockClientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);

        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(content().string(readDto));

        verify(mockClientService, times(1)).findByEmail(anyString());
        verify(mockClientService, times(1)).saveClient(any(ClientCreateDto.class));
    }
}