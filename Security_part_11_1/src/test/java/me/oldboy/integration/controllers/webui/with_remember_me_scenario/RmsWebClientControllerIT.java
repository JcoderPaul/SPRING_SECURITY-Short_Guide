package me.oldboy.integration.controllers.webui.with_remember_me_scenario;

import jakarta.servlet.http.Cookie;
import lombok.SneakyThrows;
import me.oldboy.integration.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class RmsWebClientControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    private MvcResult loginResult, anotherResult, withoutReadResult;
    private Cookie rememberMeCookie, withReadAuthRmbToken, withOutReadAuth, notValidCookie;

    @BeforeEach
    @SneakyThrows
    void setUp(){
        /* Шаг 1. - Имитируем запрос на аутентификацию и получаем ответ для обработки */
        loginResult = mockMvc.perform(post("/webui/login")
                        .with(csrf())   // Не забываем по CSRF
                        .param("username",EXIST_EMAIL)
                        .param("password",TEST_PASS)
                        .param("remember-me","true"))
                .andReturn();

        anotherResult = mockMvc.perform(post("/webui/login")
                        .with(csrf())   // Не забываем по CSRF
                        .param("username",EXIST_EMAIL_WITH_READ_AUTH)
                        .param("password",TEST_PASS)
                        .param("remember-me","true"))
                .andReturn();

        withoutReadResult = mockMvc.perform(post("/webui/login")
                        .with(csrf())   // Не забываем по CSRF
                        .param("username","user3@test.com")
                        .param("password",TEST_PASS)
                        .param("remember-me","true"))
                .andReturn();

        /* Шаг 2. - Получаем разные значения RememberMe token-a */
        rememberMeCookie = loginResult.getResponse().getCookie("remember-me");
        withReadAuthRmbToken = anotherResult.getResponse().getCookie("remember-me");
        withOutReadAuth = withoutReadResult.getResponse().getCookie("remember-me");
        notValidCookie = new Cookie("remember-me","not_valid_token");
    }

    /* Прогоняем метод через тест с имитацией работы RememberMe токена */
    @Test
    @SneakyThrows
    void clientAccount_WithRememberMeEnabled_AndValidToken_Test() {
        /* Шаг 3. - Проверяем (утверждаем) RememberMe token не пустой */
        assertThat(rememberMeCookie).isNotNull();

        /* Шаг 4. - Проводим запрос на тестируемый endpoint с полученным RememberMe token-ом, получаем отображение и содержание */
        mockMvc.perform(get("/webui/account")
                                .cookie(rememberMeCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/account.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Account</title>")))
                .andExpect(content().string(containsString("admin@test.com")))
                .andExpect(content().string(containsString("Malkolm")))
                .andExpect(content().string(containsString("Stone")))
                .andExpect(content().string(containsString("<span>19</span>")))
                .andExpect(content().string(containsString("Lovervill")))
                .andExpect(content().string(containsString("<span>33425643</span>")))
                .andExpect(content().string(containsString("Roksburg st.")))
                .andExpect(content().string(containsString("<span>555-5456-23-55</span>")))
                .andExpect(content().string(containsString("<span>+7-222-34-23456</span>")));
    }

    /* Далее пошаговая логика проверки всех методов похожа */

    @Test
    @SneakyThrows
    void clientAccount_ShouldReturnRedirect_AndNotValidToken_Test() {
        assertThat(notValidCookie).isNotNull();

        mockMvc.perform(get("/webui/account")
                        .cookie(notValidCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    void clientContact_ShouldReturnOk_WithValidToken_Test() {
        assertThat(rememberMeCookie).isNotNull();

        mockMvc.perform(get("/webui/contacts")
                        .cookie(rememberMeCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/contacts.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Contacts</title>")))
                .andExpect(content().string(containsString("Город:")))
                .andExpect(content().string(containsString("Lovervill")))
                .andExpect(content().string(containsString("Индекс:")))
                .andExpect(content().string(containsString("<span>33425643</span>")))
                .andExpect(content().string(containsString("Адрес:")))
                .andExpect(content().string(containsString("Roksburg st.")))
                .andExpect(content().string(containsString("Дом:")))
                .andExpect(content().string(containsString("<span>132</span>")))
                .andExpect(content().string(containsString("Квартира:")))
                .andExpect(content().string(containsString("<span>32</span>")))
                .andExpect(content().string(containsString("Домашний телефон:")))
                .andExpect(content().string(containsString("<span>555-5456-23-55</span>")))
                .andExpect(content().string(containsString("Мобильный телефон:")))
                .andExpect(content().string(containsString("<span>+7-222-34-23456</span>")))
                .andExpect(content().string(containsString("Электронный адрес:")))
                .andExpect(content().string(containsString("admin@test.com")));
    }

    @Test
    @SneakyThrows
    void clientContact_ShouldReturnRedirectToLogin_WithNoValidToken_Test() {
        assertThat(notValidCookie).isNotNull();

        mockMvc.perform(get("/webui/contacts")
                        .cookie(notValidCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    void clientBalance_ShouldReturnSomeRecord_AndOkWithValidToken_Test() {
        assertThat(rememberMeCookie).isNotNull();

        mockMvc.perform(get("/webui/balance")
                        .cookie(rememberMeCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/balance.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Balance</title>")))
                .andExpect(content().string(containsString("Ваш аккаунт:")))
                .andExpect(content().string(containsString("<span>186576453434</span>")))
                .andExpect(content().string(containsString("Transaction date: "+ LocalDate.now().minusDays(7) + ", " +
                        "Transaction summary: Coffee Shop, " +
                        "Transaction type: Withdrawal, " +
                        "Transaction amount: 30, " +
                        "Closing balance: 34500, " +
                        "Create date: " + LocalDate.now().minusDays(7))));
    }

    @Test
    @SneakyThrows
    void clientBalance_ShouldReturnRedirect_WithoutValidToken_Test() {
        assertThat(notValidCookie).isNotNull();

        mockMvc.perform(get("/webui/balance")
                        .cookie(notValidCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    void clientCards_ShouldReturnRecord_ClientReadAuthToken_Test() {
        assertThat(withReadAuthRmbToken).isNotNull();

        mockMvc.perform(get("/webui/cards")
                        .cookie(withReadAuthRmbToken))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/cards.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Cards</title>")))
                .andExpect(content().string(containsString("Ваш аккаунт:")))
                .andExpect(content().string(containsString("<span>273586453434</span>")));
    }

    @Test
    @SneakyThrows
    void clientCards_ShouldReturnRedirect_WithoutReadAuthToken_Test() {
        assertThat(withOutReadAuth).isNotNull();

        mockMvc.perform(get("/webui/cards")
                        .cookie(withOutReadAuth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    void clientLoans_ShouldReturnOk_WithRememberMeToken_Test() {
        assertThat(rememberMeCookie).isNotNull();

        mockMvc.perform(get("/webui/loans")
                        .cookie(rememberMeCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/loans.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Loans</title>")))
                .andExpect(content().string(containsString("Ваш аккаунт:")))
                .andExpect(content().string(containsString("<span>186576453434</span>")))
                .andExpect(content().string(containsString("Кредит - start date: 2020-10-13, " +
                        "type: Home, " +
                        "total loan: 200000, " +
                        "amount paid: 50000, " +
                        "outstanding amount: 150000, " +
                        "create date: 2020-10-13")));
    }

    @Test
    @SneakyThrows
    void clientLoans_ShouldReturnRedirectToLogin_WithoutValidRmToken_Test() {
        assertThat(notValidCookie).isNotNull();

        mockMvc.perform(get("/webui/loans")
                        .cookie(notValidCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andExpect(content().string(""));
    }
}