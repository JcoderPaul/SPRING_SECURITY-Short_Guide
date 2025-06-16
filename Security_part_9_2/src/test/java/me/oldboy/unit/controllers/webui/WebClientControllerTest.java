package me.oldboy.unit.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.controllers.webui.WebClientController;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.models.money.Account;
import me.oldboy.repository.*;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.Optional;

import static me.oldboy.constants.SecurityConstants.JWT_HEADER;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebClientController.class)
class WebClientControllerTest {

    @MockBean
    private ClientService mockClientService;
    @MockBean
    private DataSource mockDataSource;
    @MockBean
    private ClientRepository mockClientRepository;
    @MockBean
    private ClientDetailsService mockClientDetailsService;
    @MockBean
    private AuthenticationEventListener mockAuthenticationEventListener;
    @MockBean
    private Authentication mockAuthentication;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactRepository mockContactRepository;
    @MockBean
    private BalanceRepository mockBalanceRepository;
    @MockBean
    private CardRepository mockCardRepository;
    @MockBean
    private LoanRepository mockLoanRepository;

    private String testJwt;
    private Client testClient;
    private Account testAccount;

    @BeforeEach
    void setTestData(){
        testJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImF1dGhvcml0aWVzIjoiTU" +
                "9SRSBCQUQgQUNUSU9OLEFETUlOIn0.LImgJxRtwjdDFPdy5od6W5AgeDV7o7t-gAq-vFwqGwY";

        testAccount = Account.builder()
                .accountId(1L)
                .accountNumber(123456789L)
                .client(testClient)
                .build();

        testClient = Client.builder()
                .id(1L)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .details(null)
                .account(testAccount)
                .build();
    }

    /*
    Фактически данный тест можно назвать "универсальным" для текущего набора методов, т.к. при
    отсутствии аутентификации с любого из тестируемых endpoint-ов будет редирект на страницу
    логина, при этом код теста будет таким же, сменятся только тестируемые URL-ы. Поэтому мы
    просто продемонстрируем, как можно проверить остальные методы данного класса в ситуации
    обращения к ним не аутентифицированного пользователя.
    */
    @Test
    @SneakyThrows
    void shouldRedirectToLoginPage_ClientAccountWithoutAuth_Test() {
        mockMvc.perform(get("/webui/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andExpect(content().string(containsString("")));
    }

    @Test
    @SneakyThrows
    void shouldReturnAccountPage_ClientAccount_Test() {
        when(mockClientService.findByEmail(EXIST_EMAIL)).thenReturn(Optional.of(testClient));
        when(mockAuthentication.getName()).thenReturn(EXIST_EMAIL);

        mockMvc.perform(get("/webui/account")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("main_items/account.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Account</title>")));
    }

    @Test
    @SneakyThrows
    void shouldReturnContactsPage_ClientContact_Test() {
        when(mockClientService.findByEmail(EXIST_EMAIL)).thenReturn(Optional.of(testClient));
        when(mockAuthentication.getName()).thenReturn(EXIST_EMAIL);

        mockMvc.perform(get("/webui/contacts")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("main_items/contacts.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Contacts</title>")));
    }

    @Test
    @SneakyThrows
    void shouldReturnBalancePage_ClientBalance_Test() {
        when(mockClientService.findByEmail(EXIST_EMAIL)).thenReturn(Optional.of(testClient));
        when(mockAuthentication.getName()).thenReturn(EXIST_EMAIL);

        mockMvc.perform(get("/webui/balance")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("main_items/balance.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Balance</title>")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS, authorities = {"READ"})
    void shouldReturnCardPage_WithReadAuth_ClientCards_Test() {
        when(mockClientService.findByEmail(EXIST_EMAIL)).thenReturn(Optional.of(testClient));
        when(mockAuthentication.getName()).thenReturn(EXIST_EMAIL);

        mockMvc.perform(get("/webui/cards"))
                .andExpect(status().isOk())
                .andExpect(view().name("main_items/cards.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Cards</title>")));
    }

    @Test
    @SneakyThrows
    void shouldReturnLoansPage_ClientLoans_Test() {
        when(mockClientService.findByEmail(EXIST_EMAIL)).thenReturn(Optional.of(testClient));
        when(mockAuthentication.getName()).thenReturn(EXIST_EMAIL);

        mockMvc.perform(get("/webui/loans")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("main_items/loans.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Loans</title>")));
    }
}