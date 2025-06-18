package me.oldboy.unit.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.api.AccountController;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.models.money.Account;
import me.oldboy.services.AccountService;
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

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AccountController.class})
class AccountControllerTest {

    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private UserDetailsDetector userDetailsDetector;
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;
    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private AccountReadDto testAccountReadDto;
    private Client testClient;
    private Account testAccount;
    private Long testId, testAccountNumber;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        /*
            Инициализируем Json "объект конвертор" и добавляем модуль
            "корректной работы со временем" в наш конвертор объектов,
            что позволяет ему правильно обрабатывать LocalDate
        */
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testId = 1L;
        testAccountNumber = 2314523434L;

        testAccount = Account.builder()
                .accountId(testId)
                .client(testClient)
                .accountNumber(testAccountNumber)
                .build();
        testClient = Client.builder()
                .id(testId)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .account(testAccount)
                .build();
        testAccountReadDto = AccountReadDto.builder()
                .accountNumber(testAccountNumber)
                .accountType("Saving")
                .branchAddress("King's Cross 9 3/4")
                .createDt(LocalDate.of(1879, 04, 06))
                .build();

        mockClientDetails = new SecurityClientDetails(testClient);
        mockAuthentication = new TestingAuthenticationToken(mockClientDetails, TEST_PASS, mockClientDetails.getAuthorities());
    }

    @Test
    @SneakyThrows
    public void shouldReturnOk_GetAccountDetailsTest() {
        /* Мокаем безопасность */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* Мокаем логику */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(true);
        when(userDetailsDetector.getClientId()).thenReturn(testId);
        when(accountService.readAccountByClientId(testId)).thenReturn(testAccountReadDto);

        /* Мокаем запрос и ожидаемый ответ */
        MvcResult mvcResult = mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        /*
            Ранее мы грубо извлекали строковые значения результатов запроса и
            "подставляли в ожидаемый ответ", сделаем немного по-другому. Для
            начала получим результат как строку:
        */
        String strResult = mvcResult.getResponse().getContentAsString();

        /* Теперь распарсим результат response, ранее полученный как строка, в AccountReadDto */
        AccountReadDto accountFromResponse = objectMapper.readValue(strResult, AccountReadDto.class);

        /* Сличаем исходный AccountReadDto и возвращенный из response после "прогона" тестируемого метода */
        assertThat(testAccountReadDto).isEqualTo(accountFromResponse);
    }

    /* В данном случае будет возврат 204 статуса и пустая страница - мы только проверяем логику метода - симулируем проблемы с UserDetails */
    @Test
    @SneakyThrows
    public void shouldReturn_2xx_AndEmptyPage_GetAccountDetailsTest() {
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(false);

        SecurityContextHolder.setContext(mockSecurityContext);

        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }
}