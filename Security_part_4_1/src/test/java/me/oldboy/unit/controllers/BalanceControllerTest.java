package me.oldboy.unit.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BalanceControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUpWebContext(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

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