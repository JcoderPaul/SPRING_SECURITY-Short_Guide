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

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    @WithMockUser // Имитируем аутентифицированного пользователя
    void shouldReturnOkWithAuthClientGetAccountDetailsTest(){
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the account details from the DB"));
    }

    @Test
    @SneakyThrows
    void shouldReturnUnAuthWithoutAuthClientGetAccountDetailsTest(){
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }
}