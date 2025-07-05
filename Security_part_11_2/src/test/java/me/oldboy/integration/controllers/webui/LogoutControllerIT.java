package me.oldboy.integration.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class LogoutControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void exitApp_ShouldReturnOk_Test() {
        mockMvc.perform(get("/webui/exit"))
                .andExpect(status().isOk())
                .andExpect(view().name("client_forms/logout.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>Logout</title>")));
    }

    /*
        Для данного теста есть существенные оговорки в случае использования KeyCloak:
        - Spring Security явно указывает, что если мы настроили logoutSuccessHandler(), то logoutSuccessUrl() игнорируется.
        OidcClientInitiatedLogoutSuccessHandler - это специализированный LogoutSuccessHandler, который предназначен для
        инициирования выхода из Identity Provider (Keycloak в данном случае) и затем перенаправления пользователя на указанный
        postLogoutRedirectUri.
        - Когда вы используете oidcLogoutSuccessHandler(), этот обработчик перехватывает процесс выхода из системы.
        Его основная задача - не просто перенаправить нас на /webui/bye, как указано в методе:

                @PostMapping("/logout")
                public String logOut(){
                    return "redirect:/webui/bye";
                }

        а сначала перенаправить браузер пользователя на end_session_endpoint Keycloak.
        - После того, как Keycloak обработает выход, он, в свою очередь, перенаправит пользователя на postLogoutRedirectUri,
        который мы установили в oidcLogoutSuccessHandler.setPostLogoutRedirectUri() при конфигурировании цепи безопасности.
        - Наш тест должен ожидать, что после POST /webui/logout будет прямое перенаправление на /webui/bye. Но, поскольку
        OidcClientInitiatedLogoutSuccessHandler активен, он не перенаправляет напрямую на /webui/bye. Вместо этого он
        инициирует перенаправление на Keycloak. А поскольку, в тестах KeyCloak не активен, то MockMvc не полностью имитирует
        цепочку перенаправлений между нашим приложением и Keycloak, и мы получаем перенаправление на / (корневой URL), который
        является поведением по умолчанию.
    */

    @Test
    @SneakyThrows
    void logOut_ShouldReturnRedirectToRoot_Test() {
        mockMvc.perform(post("/webui/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @SneakyThrows
    void buy_ShouldReturnByeHtmlPage_Test() {
        mockMvc.perform(get("/webui/bye"))
                .andExpect(status().isOk())
                .andExpect(view().name("/bye.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>BYE</title>")));
    }
}