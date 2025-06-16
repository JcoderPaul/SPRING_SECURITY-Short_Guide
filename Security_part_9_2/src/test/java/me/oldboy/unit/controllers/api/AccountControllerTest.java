package me.oldboy.unit.controllers.api;

import lombok.SneakyThrows;
import me.oldboy.controllers.api.AccountController;
import me.oldboy.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private ContactRepository contactRepository;
    @MockBean
    private BalanceRepository balanceRepository;
    @MockBean
    private CardRepository cardRepository;
    @MockBean
    private LoanRepository loanRepository;

    @Test
    @SneakyThrows
    @WithMockUser   // Имитируем аутентифицированного пользователя
    void shouldReturnOk_WithAuthClient_GetAccountDetailsTest(){
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the account details from the DB"))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

   @Test
    @SneakyThrows
    void shouldReturn_3xx_WithoutAuthClient_GetAccountDetailsTest(){
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andExpect(content().string(""));
    }
}