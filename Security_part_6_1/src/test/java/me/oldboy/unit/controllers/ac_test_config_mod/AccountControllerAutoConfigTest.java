package me.oldboy.unit.controllers.ac_test_config_mod;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Нам нужно проверить работу метода в двух сценариях, как в соседнем тесте,
но тут мы к.г. "шарашим" по полной, хотя и отключим обращение к Liquibase,
но, задействуем авто-конфигурацию.
*/
@SpringBootTest(properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc
class AccountControllerAutoConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithMockUser // Имитируем аутентифицированного пользователя
    void shouldReturnOkWithAuthClientGetAccountDetailsTest(){
        mockMvc.perform(get("/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the account details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturnUnAuthWithoutAuthClientGetAccountDetailsTest(){
        mockMvc.perform(get("/myAccount"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }
}