package me.oldboy.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoticesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /*
    Явно пробросим исключение. К данному endpoint-у могут получить доступ все и
    поэтому особых настроек по имитации аутентифицированного пользователя тут нет.
    */
    @Test
    void shouldReturnOkWithoutAuthGetNotices() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("Here are the notices details from the DB");
    }
}