package me.oldboy.controllers.api;

import lombok.SneakyThrows;
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
        mockMvc.perform(get("/myCards"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Here are the card details from the DB"));
    }

    /*
        Раньше при отсутствии аутентификации мы получали ответ - 4хх, теперь, при текущих
        настройках цепи безопасности, будет перенаправление запроса или ответ сервера 3хх.
    */
    @Test
    @SneakyThrows
    void shouldReturn_3xx_WithoutAuthUser_GetCardDetailsTest() {
        mockMvc.perform(get("/myCards"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""))
                .andExpect(redirectedUrlPattern("**/login"));
    }
}