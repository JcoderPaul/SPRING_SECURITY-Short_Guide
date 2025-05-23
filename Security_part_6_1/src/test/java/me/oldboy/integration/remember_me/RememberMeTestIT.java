package me.oldboy.integration.remember_me;

import jakarta.servlet.http.Cookie;
import lombok.SneakyThrows;
import me.oldboy.integration.TestContainerInit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RememberMeTestIT extends TestContainerInit {

    @Autowired
    private MockMvc mockMvc;

    /* Имитируем установку галочки в форму */
    @Test
    @SneakyThrows
    void shouldReturn_3xx_And_RememberMeCookie_Test() {
        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .param("username", EXIST_EMAIL)
                        .param("password", TEST_PASS)
                        .param("remember-me", "true"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie rememberMeCookie = mvcResult.getResponse().getCookie("remember-me");

        assertThat(rememberMeCookie).isNotNull();
        assertThat(rememberMeCookie.getMaxAge() > 0).isTrue();
        assertThat(rememberMeCookie.getValue().isEmpty()).isFalse();
    }

    /* Имитируем отправку формы без галочки */
    @Test
    @SneakyThrows
    void shouldReturn_3xx_And_ZeroRememberMeCookie_Test() {
        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .param("username", EXIST_EMAIL)
                        .param("password", TEST_PASS)
                        .param("remember-me", "false"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie rememberMeCookie = mvcResult.getResponse().getCookie("remember-me");

        assertThat(rememberMeCookie).isNull();
    }
}