package me.oldboy.unit.controllers.api;

import lombok.SneakyThrows;
import me.oldboy.controllers.api.CardsController;
import me.oldboy.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardsController.class)
class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    @WithMockUser
    void shouldReturn_2xx_WithAuthUser_GetCardDetailsTest() {
        mockMvc.perform(get("/api/myCards"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Here are the card details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturn_3xx_WithoutAuthUser_GetCardDetailsTest() {
        mockMvc.perform(get("/api/myCards"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }
}