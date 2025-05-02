package me.oldboy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/*
Эта аннотация фокусируется конкретно на тестировании веб-слоя. Она автоматически настраивает инфраструктуру Spring MVC
и ограничивает загруженные компоненты только теми, которые имеют отношение к веб-тестированию (например, наш контроллер,
@ControllerAdvice, фильтры и WebMvcConfigurer)
*/
@WebMvcTest(WelcomeController.class)
/* Имитируем ввод учетных данных на все тесты в текущем классе, т.к. у нас включена защита endpoint-ов */
@WithMockUser(username = "admin@test.com", password = "test", authorities = {"ADMIN"})
class WelcomeControllerTest {

    @Autowired
    private MockMvc mockMvc; // Предоставляет возможность выполнения HTTP-запросов

    @Test
    public void testHelloEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/welcome")) // Имитируем GET запрос к /welcome
                .andExpect(MockMvcResultMatchers.status().isOk()) // Ожидаем HTTP статус - 200 OK
                .andExpect(MockMvcResultMatchers.content().string("Welcome from Spring Application with Security")); // Ожидаем в ответ response body
    }
}