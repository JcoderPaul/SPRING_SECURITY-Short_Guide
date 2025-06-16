package me.oldboy.unit.controllers.api;

import me.oldboy.controllers.api.CardsController;
import me.oldboy.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardsController.class)
class CardsControllerTest {

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
    @WithMockUser(authorities = "READ")
    void shouldReturnOk_WithReadAuthClient_GetCardDetailsTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/myCards"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("Here are the card details from the DB");
    }

    @Test
    void shouldReturn_3xx_WithoutAuthUser_GetCardDetailsTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/myCards"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("");
    }
}