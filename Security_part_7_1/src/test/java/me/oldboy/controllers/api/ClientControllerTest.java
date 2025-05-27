package me.oldboy.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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

@WebMvcTest(ClientController.class)
class ClientControllerTest {

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

    /*
        Три следующих теста проверяют "/helloAdmin" endpoint по трем основным сценариям:
        - пользователь аутентифицирован и он имеет роль (привилегии) ADMIN - все ок;
        - пользователь аутентифицирован и он имеет роль (привилегии) USER - в доступе отказано;
        - пользователь не аутентифицирован - перенаправление на страницу логина;
    */
    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, authorities = {TEST_STR_ROLE_ADMIN})
    void shouldReturnWelcomeString_AdminWithAuthUser_HelloAdminTest() {
        String expected = "This page for ADMIN only! \nHello: " + EXIST_EMAIL;

        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {TEST_STR_ROLE_USER})
    void shouldReturn_IsForbidden_WithOutAuthAdmin_HelloAdminTest() {
        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void shouldReturn_4xx_WithOutAuth_HelloAdminTest() {
        mockMvc.perform(get("/api/admin/helloAdmin"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {TEST_STR_ROLE_ADMIN})
    void shouldReturnOk_AndTestedList_ForAuthAdmin_GetAllClientTest(){
        when(mockClientService.findAll()).thenReturn(testDtoList);
        /* Выводить полный список смысла нет, возьмем только наглядную часть */
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("{\"email\":\"for_test@test.com\"," +
                                "\"role\":\"USER\"," +
                                "\"clientName\":\"Testino\"," +
                                "\"clientSurName\":\"Testorelly\"," +
                                "\"age\":32}")));
    }

    /*
        Следующие два теста на POST запрос и поскольку у нас в цепи безопасности
        есть защита от CSRF атак, нам нужно имитировать и процесс передачи токена.
    */
    @Test
    @WithMockUser(authorities = {TEST_STR_ROLE_ADMIN})
    void shouldReturn_ValidationErrors_RegistrationClientTest() throws Exception {
        String notValidDto = objectMapper.writeValueAsString(notValidCreateClientDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidDto))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {TEST_STR_ROLE_ADMIN})
    void shouldReturn_ValidationOk_RegistrationClientTest() throws Exception {
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);
        String readDto = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(testClientReadDto);

        when(mockClientService.findByEmail(testClientCreateDto.getEmail())).thenReturn(Optional.empty());
        when(mockClientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);

        mockMvc.perform(post("/api/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(content().string(readDto));

        verify(mockClientService, times(1)).findByEmail(anyString());
        verify(mockClientService, times(1)).saveClient(any(ClientCreateDto.class));
    }
}