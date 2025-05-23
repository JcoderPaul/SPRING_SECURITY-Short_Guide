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

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private DataSource dataSource;

    @Test
    @SneakyThrows
    @WithMockUser
    void shouldReturn_2xx_WithAuthClient_getBalanceDetails() {
        mockMvc.perform(get("/myBalance"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Here are the balance details from the DB"));
    }

    /*
        Раньше при отсутствии аутентификации мы получали ответ - 4хх, теперь, при текущих
        настройках цепи безопасности, будет перенаправление запроса или ответ сервера 3хх.
    */
    @Test
    @SneakyThrows
    void shouldReturn_3xx_WithoutAuthClient_getBalanceDetails() {
        mockMvc.perform(get("/myBalance"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""))
                .andExpect(redirectedUrlPattern("**/login"))    // Мы можем указать некий ожидаемый URL паттерн, например, его окончание
                .andExpect(redirectedUrl("http://localhost/webui/login"));  // Либо мы можем указать точный адрес перенаправления
    }
}