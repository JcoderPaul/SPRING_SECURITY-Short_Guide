package me.oldboy.controller;

import lombok.SneakyThrows;
import me.oldboy.controllers.AccountController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String userName = "admin@test.com";

    /*
    В прошлом примере мы применили аннотацию @WithMockUser над тестовым классом, немного изменим подход.
    Теперь используем настройку методом инициализации и аннотацией @BeforeEach. Т.е. теперь перед каждым
    тестовым методом мы будем "получать" тестового пользователя.
    */
    @BeforeEach
    void initTestUser() {
        /* Создаем тестового User-a и коллекцию его Role-ей */
        List<GrantedAuthority> roles = Arrays.asList(new SimpleGrantedAuthority("ADMIN"),
                                                     new SimpleGrantedAuthority("USER"));
        User testUser = new User(userName,"test", roles);

        /* Создаем тестовый аутентификационный токен, в его параметры загружаем нашего тестового пользователя, именно его будет отслеживать тестовый фильтр. */
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(testUser, testUser.getPassword(), roles);

        /* Создаем пустой контекст безопасности используя SecurityContextHolder */
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        /* Загружаем в контекст наш тестовый токен */
        securityContext.setAuthentication(authenticationToken);

        /* Загружаем получившийся контекст в хранителя контекста безопасности */
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @SneakyThrows
    void shouldReturnOkGetAccountDetailsTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/myAccount"))// Имитируем GET запрос
                .andExpect(MockMvcResultMatchers.status().isOk()) // Ожидаем HTTP статус - 200 OK
                .andExpect(MockMvcResultMatchers.content().string("Here are the account details from the DB")); // Ожидаем в ответ response body
    }
}