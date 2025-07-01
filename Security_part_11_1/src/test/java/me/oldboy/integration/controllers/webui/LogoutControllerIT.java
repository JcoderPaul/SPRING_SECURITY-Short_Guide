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

    @Test
    @SneakyThrows
    void logOut_ShouldReturnRedirectToByePage_Test() {
        mockMvc.perform(post("/webui/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/bye"));
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