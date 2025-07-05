package me.oldboy.integration.controllers.webui.with_user_details_scenario;

import lombok.SneakyThrows;
import me.oldboy.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL_WITH_READ_AUTH;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UdsWebClientControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    /*
        Прогоняем метод через тест с использованием аннотации @WithUserDetails,
        игнорируем RememberMe и OAuth2 - базовый сценарий, мы просто имитируем
        уже аутентифицированного клиента для проверки логики и интеграции метода.
    */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void clientAccount_ShouldReturnOk_WithUserDetails_Test() {
        mockMvc.perform(get("/webui/account"))
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

    @Test
    @SneakyThrows
    void clientAccount_ShouldRedirectToLogin_WithoutAuthClient_Test() {
        mockMvc.perform(get("/webui/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void clientContact_ShouldReturnOk_WithAuthClient_Test() {
        mockMvc.perform(get("/webui/contacts"))
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
    void clientContact_ShouldReturnRedirectToLogin_WithoutAuth_Test() {
        mockMvc.perform(get("/webui/contacts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void clientBalance_ShouldReturnSomeRecord_AndOk_Test() {
        mockMvc.perform(get("/webui/balance"))
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
                        "Create date: " + LocalDate.now().minusDays(7))));   // Для тестов проверим наличие только одной транзакции хотя их больше
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "user3@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void clientBalance_ShouldReturnZeroRecord_IfClientHasNoTransaction_AndOk_Test() {
        mockMvc.perform(get("/webui/balance"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/balance.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Balance</title>")))
                .andExpect(content().string(containsString("Ваш аккаунт:")))
                .andExpect(content().string(containsString("<span>853577453434</span>")))
                .andExpect(content().string(not(containsString("Transaction date: "))))
                .andExpect(content().string(not(containsString("Transaction summary: "))))
                .andExpect(content().string(not(containsString("Transaction type: "))))
                .andExpect(content().string(not(containsString("Transaction amount: "))))
                .andExpect(content().string(not(containsString("Closing balance: "))))
                .andExpect(content().string(not(containsString("Create date: "))));
    }

    @Test
    @SneakyThrows
    void clientBalance_ShouldReturnRedirectToLogin_NotAuthClient_Test() {
        mockMvc.perform(get("/webui/balance"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    /* У нас только один пользователь с допуском READ, и у него нет карт, но страницу мы просмотреть можем */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL_WITH_READ_AUTH, userDetailsServiceBeanName = "clientDetailsService")
    void clientCards_ShouldReturnCardsPage_WithReadAuth_Test() {
        mockMvc.perform(get("/webui/cards"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/cards.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Cards</title>")))
                .andExpect(content().string(containsString("Ваш аккаунт:")))
                .andExpect(content().string(containsString("<span>273586453434</span>")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "user3@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void clientCards_ShouldReturnForbidden_WithoutReadAuth_Test() {
        mockMvc.perform(get("/webui/cards"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void clientCards_ShouldReturnRedirect_WithoutAuth_Test() {
        mockMvc.perform(get("/webui/cards"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void clientLoans_ShouldReturnOk_AndSomeLoanRecord_Test() {
        mockMvc.perform(get("/webui/loans"))
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
                        "create date: 2020-10-13")));   // Для тестов проверим наличие только одного кредита хотя их больше
    }

    @Test
    @SneakyThrows
    void clientLoans_ShouldReturnRedirectToLogin_WithoutAuthClient_Test() {
        mockMvc.perform(get("/webui/loans"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andExpect(content().string(""));
    }
}