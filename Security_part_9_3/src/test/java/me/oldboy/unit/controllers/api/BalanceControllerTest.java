package me.oldboy.unit.controllers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.TestWebAppInitializer;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.api.BalanceController;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.services.BalanceService;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestWebAppInitializer.class})
class BalanceControllerTest {

    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private BalanceService balanceService;
    @MockitoBean
    private UserDetailsDetector userDetailsDetector;

    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;

    @InjectMocks
    private BalanceController balanceController;

    private MockMvc mockMvc;
    private Client testClient;
    private TransactionReadDto testTransactionReadDto;
    private List<TransactionReadDto> testList;
    private Long testId;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        /*
        Инициализируем Json "объект конвертор" и добавляем модуль
        "корректной работы со временем" в наш конвертор объектов,
        что позволяет ему правильно обрабатывать LocalDate
        */
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(balanceController).build();

        testId = 1L;

        testClient = Client.builder()
                .id(testId)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .build();

        testTransactionReadDto = TransactionReadDto.builder()
                .createDt(LocalDate.of(2023,03,12))
                .transactionType("Pay")
                .transactionDt(LocalDate.of(2023,03,14))
                .transactionAmt(30)
                .closingBalance(32122)
                .build();

        mockClientDetails = new SecurityClientDetails(testClient);
        mockAuthentication = new TestingAuthenticationToken(mockClientDetails, TEST_PASS, mockClientDetails.getAuthorities());

        testList = List.of(testTransactionReadDto);
    }

    @Test
    @SneakyThrows
    void shouldReturnOk_AndTestList_GetBalanceDetails_Test() {
        /* Мокаем безопасность */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* Мокаем логику */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(true);
        when(userDetailsDetector.getClientId()).thenReturn(testId);
        when(balanceService.readAllTransactionByClientId(testId)).thenReturn(testList);

        /* Мокаем запрос и ожидаемый ответ */
        MvcResult mvcResult = mockMvc.perform(get("/api/myBalance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        /*
        Ранее мы грубо извлекали строковые значения результатов запроса и
        "подставляли в ожидаемый ответ", сделаем немного по-другому. Для
        начала получим результат как строку:
        */
        String strResult = mvcResult.getResponse().getContentAsString();

        /* Теперь распарсим результат response, ранее полученный как строку, в List */
        List<TransactionReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<TransactionReadDto>>() {});

        /* Сличаем единственные элементы исходного и возвращенного List-a на соответствие */
        assertThat(listFromResponse.get(0)).isEqualTo(testList.get(0));
    }

    @Test
    @SneakyThrows
    void shouldReturn_2xx_AndEmptyPage_GetBalanceDetails_Test() {
        /* Мокаем безопасность */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* Мокаем логику */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(false);

        /* Мокаем запрос и ожидаемый ответ */
        mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }
}