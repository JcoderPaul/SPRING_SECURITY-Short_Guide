package me.oldboy.controller;

import lombok.SneakyThrows;
import me.oldboy.controllers.BalanceController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@test.com", password = "test", authorities = {"ADMIN"})
    void shouldReturnOkGetBalanceDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myBalance"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Here are the balance details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturnNotAuthorizedGetBalanceDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myBalance"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}