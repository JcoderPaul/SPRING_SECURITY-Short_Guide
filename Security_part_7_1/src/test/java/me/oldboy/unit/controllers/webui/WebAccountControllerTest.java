package me.oldboy.unit.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.controllers.webui.WebAccountController;
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

@WebMvcTest(WebAccountController.class)
class WebAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    @WithMockUser
    void showAccountToWebPage_WithAuth_MyAccount_Test() {
        mockMvc.perform(get("/webui/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("My account data from DB"));
    }

    /*
        Раньше при отсутствии аутентификации мы получали ответ - 4хх, теперь, при текущих настройках
        отдельной цепи безопасности для web, будет перенаправление запроса или ответ сервера 3хх.
    */
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