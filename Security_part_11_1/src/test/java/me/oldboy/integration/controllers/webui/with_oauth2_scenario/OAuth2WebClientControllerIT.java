package me.oldboy.integration.controllers.webui.with_oauth2_scenario;

import lombok.SneakyThrows;
import me.oldboy.config.AppSecurityConfig;
import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.integration.IntegrationTestBase;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_CLIENT_NAME;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class OAuth2WebClientControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientDetailsService clientDetailsService;
    private UserDetails userDetails;
    private Set<Method> userDetailsMethods;
    private OAuth2User oAuth2UserReturn;
    private OidcUser oidcUserReturn;
    private OAuth2User oAuth2UserForProxy;
    private DefaultOidcUser oidcUserForProxy;

    @BeforeEach
    void setUp(){
        userDetails = clientDetailsService.loadUserByUsername(EXIST_EMAIL);
        userDetailsMethods = Set.of(UserDetails.class.getMethods());

        Map<String, Object> attributes = Map.of("sub", EXIST_EMAIL,"name", TEST_CLIENT_NAME,"email", EXIST_EMAIL);
        OidcIdToken idToken = new OidcIdToken("mock-token-value",
                Instant.now(),
                Instant.now().plusSeconds(360000),
                attributes);

        oAuth2UserForProxy = new DefaultOAuth2User(userDetails.getAuthorities(), attributes,"email");
        oidcUserForProxy = new DefaultOidcUser(userDetails.getAuthorities(), idToken);

        oAuth2UserReturn = (OAuth2User) Proxy.newProxyInstance(AppSecurityConfig.class.getClassLoader(),
                new Class[]{UserDetails.class, OAuth2User.class},
                (proxy, method, args) -> userDetailsMethods.contains(method)
                        ? method.invoke(userDetails, args)
                        : method.invoke(oAuth2UserForProxy, args));

        oidcUserReturn = (OidcUser) Proxy.newProxyInstance(AppSecurityConfig.class.getClassLoader(),
                new Class[]{UserDetails.class, OidcUser.class},
                (proxy, method, args) -> userDetailsMethods.contains(method)
                        ? method.invoke(userDetails, args)
                        : method.invoke(oidcUserForProxy, args));
    }

    /* Прогоняем метод через тест с имитацией работы OAuth2 */
    @Test
    @SneakyThrows
    void clientAccount_ShouldReturnOk_WithOAuth2_Test() {
        mockMvc.perform(get("/webui/account")
                        .with(oauth2Login().oauth2User(oAuth2UserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/account.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Account</title>")))
                .andExpect(content().string(Matchers.containsString("admin@test.com")))
                .andExpect(content().string(Matchers.containsString("Malkolm")))
                .andExpect(content().string(Matchers.containsString("Stone")))
                .andExpect(content().string(Matchers.containsString("<span>19</span>")))
                .andExpect(content().string(Matchers.containsString("Lovervill")))
                .andExpect(content().string(Matchers.containsString("<span>33425643</span>")))
                .andExpect(content().string(Matchers.containsString("Roksburg st.")))
                .andExpect(content().string(Matchers.containsString("<span>555-5456-23-55</span>")))
                .andExpect(content().string(Matchers.containsString("<span>+7-222-34-23456</span>")));
    }

    @Test
    @SneakyThrows
    void clientAccount_ShouldReturnOk_WithOidcUser_Test() {
        mockMvc.perform(get("/webui/account")
                        .with(oauth2Login().oauth2User(oidcUserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/account.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Account</title>")));
    }

    @Test
    @SneakyThrows
    void clientContact_ShouldReturnOk_WithAuth2Client_Test() {
        mockMvc.perform(get("/webui/contacts")
                        .with(oauth2Login().oauth2User(oAuth2UserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/contacts.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Contacts</title>")))
                .andExpect(content().string(Matchers.containsString("Город:")))
                .andExpect(content().string(Matchers.containsString("Lovervill")))
                .andExpect(content().string(Matchers.containsString("Индекс:")))
                .andExpect(content().string(Matchers.containsString("<span>33425643</span>")))
                .andExpect(content().string(Matchers.containsString("Адрес:")))
                .andExpect(content().string(Matchers.containsString("Roksburg st.")))
                .andExpect(content().string(Matchers.containsString("Дом:")))
                .andExpect(content().string(Matchers.containsString("<span>132</span>")))
                .andExpect(content().string(Matchers.containsString("Квартира:")))
                .andExpect(content().string(Matchers.containsString("<span>32</span>")))
                .andExpect(content().string(Matchers.containsString("Домашний телефон:")))
                .andExpect(content().string(Matchers.containsString("<span>555-5456-23-55</span>")))
                .andExpect(content().string(Matchers.containsString("Мобильный телефон:")))
                .andExpect(content().string(Matchers.containsString("<span>+7-222-34-23456</span>")))
                .andExpect(content().string(Matchers.containsString("Электронный адрес:")))
                .andExpect(content().string(Matchers.containsString("admin@test.com")));
    }

    @Test
    @SneakyThrows
    void clientContact_ShouldReturnOk_WithOidc_Test() {
        mockMvc.perform(get("/webui/contacts")
                        .with(oauth2Login().oauth2User(oidcUserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/contacts.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Contacts</title>")));
    }

    @Test
    @SneakyThrows
    void clientBalance_ShouldReturnSomeRecord_AndOk_WithOAuth2_Test() {
        mockMvc.perform(get("/webui/balance")
                        .with(oauth2Login().oauth2User(oAuth2UserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/balance.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Balance</title>")))
                .andExpect(content().string(Matchers.containsString("Ваш аккаунт:")))
                .andExpect(content().string(Matchers.containsString("<span>186576453434</span>")))
                .andExpect(content().string(Matchers.containsString("Transaction date: "+ LocalDate.now().minusDays(7) + ", " +
                        "Transaction summary: Coffee Shop, " +
                        "Transaction type: Withdrawal, " +
                        "Transaction amount: 30, " +
                        "Closing balance: 34500, " +
                        "Create date: " + LocalDate.now().minusDays(7))));   // Для тестов проверим наличие только одной транзакции хотя их больше
    }

    @Test
    @SneakyThrows
    void clientBalance_ShouldReturnSomeRecord_AndOk_WithOidc_Test() {
        mockMvc.perform(get("/webui/balance")
                        .with(oauth2Login().oauth2User(oidcUserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/balance.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Balance</title>")))
                .andExpect(content().string(Matchers.containsString("Ваш аккаунт:")));
    }

    @Test
    @SneakyThrows
    void clientCards_ShouldReturnCardsPage_WithOAuth2_Test() {
        mockMvc.perform(get("/webui/cards")
                        .with(oauth2Login().oauth2User(oAuth2UserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/cards.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Cards</title>")))
                .andExpect(content().string(Matchers.containsString("Ваш аккаунт:")))
                .andExpect(content().string(Matchers.containsString("<span>186576453434</span>")));
    }

    @Test
    @SneakyThrows
    void clientCards_ShouldReturnCardsPage_WithOidc_Test() {
        mockMvc.perform(get("/webui/cards")
                        .with(oauth2Login().oauth2User(oidcUserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/cards.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Cards</title>")))
                .andExpect(content().string(Matchers.containsString("Ваш аккаунт:")))
                .andExpect(content().string(Matchers.containsString("<span>186576453434</span>")));
    }

    @Test
    @SneakyThrows
    void clientLoans_ShouldReturnOk_AndSomeLoanRecord_WithOAuth2_Test() {
        mockMvc.perform(get("/webui/loans")
                        .with(oauth2Login().oauth2User(oAuth2UserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/loans.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Loans</title>")))
                .andExpect(content().string(Matchers.containsString("Ваш аккаунт:")))
                .andExpect(content().string(Matchers.containsString("<span>186576453434</span>")))
                .andExpect(content().string(Matchers.containsString("Кредит - start date: 2020-10-13, " +
                        "type: Home, " +
                        "total loan: 200000, " +
                        "amount paid: 50000, " +
                        "outstanding amount: 150000, " +
                        "create date: 2020-10-13")));
    }

    @Test
    @SneakyThrows
    void clientLoans_ShouldReturnOk_AndSomeLoanRecord_WithOidc_Test() {
        mockMvc.perform(get("/webui/loans")
                        .with(oauth2Login().oauth2User(oidcUserReturn)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main_items/loans.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("<title>Loans</title>")));
    }
}