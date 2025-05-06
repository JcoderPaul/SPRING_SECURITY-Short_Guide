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
class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithMockUser
    void shouldReturn2xxStatusGetCardDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myCards"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Here are the card details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturn4xxStatusGetCardDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myCards"))
                .andExpect(status().is4xxClientError());
    }
}