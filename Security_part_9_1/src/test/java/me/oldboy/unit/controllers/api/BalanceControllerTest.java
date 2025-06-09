package me.oldboy.unit.controllers.api;

import me.oldboy.controllers.api.BalanceController;
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

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

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
    @WithMockUser
    void shouldReturnOk_WithAuthClient_GetBalanceDetailsTest() throws Exception {
        mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the balance details from the DB"))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    void shouldReturn_3xx_GetBalanceDetailsTest() throws Exception {
        mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andExpect(content().string(""));
    }
}