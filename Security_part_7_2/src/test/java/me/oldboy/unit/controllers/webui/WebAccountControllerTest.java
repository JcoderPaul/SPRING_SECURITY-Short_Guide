package me.oldboy.unit.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.config.security_config.AppSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppSecurityConfig.class})
@WebAppConfiguration
class WebAccountControllerTest {

    @Autowired
    private WebApplicationContext webAppContext;
    private MockMvc mockMvc;

    @BeforeEach
    void setTestData() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void showAccountToWebPage_WithAuth_MyAccount_Test() {
        mockMvc.perform(get("/webui/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("My account data from DB"));
    }

    @Test
    @SneakyThrows
    void shouldRedirectToLoginPage_WithoutAuthClient_ShowAccountToWebPage_Test(){
        mockMvc.perform(get("/webui/myAccount"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""))
                .andExpect(redirectedUrlPattern("**/login"))    // Мы можем указать некий ожидаемый URL паттерн, например, его окончание
                .andExpect(redirectedUrl("http://localhost/webui/login"));  // Либо мы можем указать точный адрес перенаправления;
    }
}