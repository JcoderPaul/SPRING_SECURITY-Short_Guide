package me.oldboy.integration.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.controllers.webui.WebClientController;
import me.oldboy.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL_WITH_READ_AUTH;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
    В unit тестах мы мокали зависимости и тестовые переменные,
    тут они работают сообща и данные тянутся из БД развернутой
    в тест контейнере.
*/
@AutoConfigureMockMvc
class WebClientControllerTestIT extends IntegrationTestBase {

    @Autowired
    private WebClientController webClientController;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnAccountPage_ClientAccount_Test() {
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
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnContactsPage_ClientContact_Test() {
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
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnBalancePage_ClientBalance_Test() {
        mockMvc.perform(get("/webui/balance"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/balance.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Balance</title>")))
                .andExpect(content().string(containsString("Ваш аккаунт:")))
                .andExpect(content().string(containsString("<span>186576453434</span>")))
                .andExpect(content().string(containsString("Transaction date: 2025-06-05, " +
                        "Transaction summary: Coffee Shop, " +
                        "Transaction type: Withdrawal, " +
                        "Transaction amount: 30, " +
                        "Closing balance: 34500, " +
                        "Create date: 2025-06-05")));   // Для тестов проверим наличие только одной транзакции хотя их больше
    }

    /* У нас только один пользователь с допуском READ, и у него нет карт, но страницу мы просмотреть можем */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL_WITH_READ_AUTH, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnCardsPage_ClientCards_Test() {
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
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldReturnCardsPage_ClientLoans_Test() {
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

    /*
        Текущие тестируемые эндпоинты защищены от доступа не аутентифицированного пользователя, и в случае попытки
        обращения к ним без должного разрешения, поведение будет одинаковым - переброска на страницу логина. Поэтому
        проверим только для одного из них, например - страница с отображением списка кредитов.
    */
    @Test
    @SneakyThrows
    void shouldRedirect_3xx_WebuiAuthEndpoint_Test() {
        mockMvc.perform(get("/webui/loans"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andExpect(content().string(""));
    }
}