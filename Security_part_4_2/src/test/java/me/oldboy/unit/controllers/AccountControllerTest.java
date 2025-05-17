package me.oldboy.unit.controllers;

import lombok.SneakyThrows;
import me.oldboy.config.AppWebInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppWebInitializer.class})
@WebAppConfiguration
class AccountControllerTest {

    @Autowired
    private WebApplicationContext webAppContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(springSecurity())    // Включаем Security для MockMvc
                .build();
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void shouldReturnOkGetAccountDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the account details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturn_4xx_WithOutAuthUser_GetAccountDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myAccount"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }
}