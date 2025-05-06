package me.oldboy.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithMockUser
    void shouldReturnOkGetBalanceDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myBalance"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the balance details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturnNotAuthorizedGetBalanceDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myBalance"))
                .andExpect(status().isUnauthorized());
    }
}