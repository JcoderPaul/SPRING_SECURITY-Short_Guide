package me.oldboy.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardsController.class)
class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /*
    Логика проверки обоих тестов схожа, слегка сократим синтаксис за счет статического
    импорта и в отличие от предыдущих тестов используем другие методы возврата статуса.
    */
    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@test.com", password = "1234", authorities = {"USER"})
    void shouldReturn2xxStatusGetCardDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myCards"))
               .andExpect(status().is2xxSuccessful())
               .andExpect(content().string("Here are the card details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturn4xxStatusGetCardDetailsTest() {
        mockMvc.perform(get("/myCards"))
               .andExpect(status().is4xxClientError());
    }
}