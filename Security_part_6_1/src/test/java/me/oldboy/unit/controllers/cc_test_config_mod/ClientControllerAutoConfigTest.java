package me.oldboy.unit.controllers.cc_test_config_mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

/* Задействуем в тесте авто-конфигурацию (чуть меньше строк кода, по сравнению с соседним набором тестов) - 10 сек. */
@SpringBootTest(properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc
@WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, authorities = {TEST_STR_ROLE_ADMIN})
class ClientControllerAutoConfigTest {

    @MockBean
    private ClientService mockClientService;
    @Autowired
    private MockMvc mockMvc;

    private List<ClientReadDto> testDtoList;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto;
    private ClientReadDto testClientReadDto;
    private static ObjectMapper objectMapper;

    /* Готовим тестовые данные */
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

    /* Тестируем приветственный endpoint для пользователя со статусом ADMIN */
    @Test
    @SneakyThrows
    void shouldReturnWelcomeStringAdminWithAuthUserNameTest() {
        mockMvc.perform(get("/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string("This page for ADMIN only! \nHello: " + EXIST_EMAIL));
    }

    /* Тестируем endpoint для просмотра списка клиентов, проверяем наличие интересующего нас */
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

    /* Тестируем endpoint регистрации клиента на отработку не валидности передаваемых данных */
    @Test
    void shouldReturn_ValidationErrors_RegistrationClientTest() throws Exception {
        String notValidDto = objectMapper.writeValueAsString(notValidCreateClientDto);

        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidDto))
                .andExpect(status().isBadRequest());
    }

    /* Тестируем endpoint регистрации клиента на отработку сохранения передаваемых валидных данных */
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