package me.oldboy.unit.controllers;

import lombok.SneakyThrows;
import me.oldboy.controllers.BalanceController;
import me.oldboy.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    @WithMockUser
    void shouldReturn_2xx_WithAuthClient_getBalanceDetails() {
        mockMvc.perform(get("/myBalance"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Here are the balance details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturn_4xx_WithoutAuthClient_getBalanceDetails() {
        mockMvc.perform(get("/myBalance"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }
}