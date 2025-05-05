package me.oldboy.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
/* Общая аннотация задает только имя и пароль, роли и привилегии задаем в каждом методе отдельно */
@WithMockUser(username = "test@test.com", password = "test")
class ClientsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"}) // Позволяем получить данные пользователю с ролью ADMIN
    void shouldReturnClientListWithAdminRoleGetClientListTest() {
        MvcResult mvcResult = mockMvc.perform(get("/clientList"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("{\"userName\":\"admin\",\"userActive\":true},{\"userName\":\"user\",\"userActive\":true}");
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"}) // Поскольку роль задана USER, то пользователь не сможет получить доступ к ресурсу
    void shouldReturn401WithUserRoleGetClientListTest() {
        MvcResult mvcResult = mockMvc.perform(get("/clientList"))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("");
    }
}