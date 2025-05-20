package me.oldboy.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoansControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithMockUser
    void shouldReturnOkWithAuthUserGetLoanDetailsTest() {
        mockMvc.perform(get("/myLoans"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the loan details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturn_Unauthorized_WithoutAuthUser_GetLoanDetailsTest() {
        mockMvc.perform(get("/myLoans"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }
}