package me.oldboy.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /* Тестируем выбранный endpoint с учетом симуляции авторизации, в ответ получим 200 статус */
    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@test.com", password = "test", authorities = {"ADMIN"})
    void shouldReturnOkGetBalanceDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myBalance"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Here are the balance details from the DB"));
    }

    /*
    Тестируем выбранный endpoint с учетом отсутствия авторизации, в ответ получим 401 статус или
    "не авторизован" - т.е. в запросе отсутствуют действительные учетные данные аутентификации для
    запрашиваемого ресурса (endpoint-a), т.к. в нашем случае он прописан в методе *.configure(),
    класса SecurityConfig, как требующий аутентификации. В первом случае мы симулировали таковую,
    тут нет.
    */
    @Test
    @SneakyThrows
    void shouldReturnNotAuthorizedGetBalanceDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myBalance"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}