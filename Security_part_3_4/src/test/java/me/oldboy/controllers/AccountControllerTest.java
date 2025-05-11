package me.oldboy.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/*
В данной ситуации мы можем применить аннотацию @AutoConfigureMockMvc и тогда код теста будет выглядеть так  :

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void shouldReturnOkWithAuthUserGetAccountDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myAccount"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.content().string("Here are the account details from the DB"));
    }
}
*/

@SpringBootTest
@WithMockUser
class AccountControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(springSecurity())    // Включаем Security для MockMvc
                .build();
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithAuthUserGetAccountDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myAccount"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.content().string("Here are the account details from the DB"));
    }
}