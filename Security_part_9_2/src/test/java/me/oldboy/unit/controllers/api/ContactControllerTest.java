package me.oldboy.unit.controllers.api;

import me.oldboy.controllers.api.ContactController;
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

@WebMvcTest(ContactController.class)
class ContactControllerTest {

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
    void shouldReturn_200_WithAuthUser_GetContactDetailsTest() throws Exception {
        mockMvc.perform(get("/api/contact"))
                .andExpect(status().isOk())
                .andExpect(content().string("Get details from DB"))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    void shouldReturn_3xx_WithoutAuthUser_GetContactDetailsTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/contact"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("");
    }
}