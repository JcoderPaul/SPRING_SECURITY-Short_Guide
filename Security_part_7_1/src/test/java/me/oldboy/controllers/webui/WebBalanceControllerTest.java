package me.oldboy.controllers.webui;

import lombok.SneakyThrows;
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

/*
    В этот раз в отличие от предыдущих, мы две цепочки безопасности: отдельно для
    REST и WEB, и тут, при обращении к защищенному endpoint-у, без аутентификации
    нас перебросит на страницу логина.
*/
@WebMvcTest(WebBalanceController.class)
class WebBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    @WithMockUser
    void showMyBalanceToWebPage_WithAuth_Test() {
        mockMvc.perform(get("/webui/myBalance"))
                .andExpect(status().isOk())
                .andExpect(content().string("My balance from DB"));
    }

    /*
    Раньше при отсутствии аутентификации мы получали ответ - 4хх, теперь, при текущих
    настройках цепи безопасности, будет перенаправление запроса или ответ сервера 3хх.
    В принципе этот ответ можно увидеть в соседних "api" контроллерах.
    */
    @Test
    @SneakyThrows
    void shouldReturn_3xx_WithoutAuthClient_MyBalanceEndPoint_Test() {
        mockMvc.perform(get("/webui/myBalance"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""))
                .andExpect(redirectedUrlPattern("**/login"))    // Мы можем указать некий ожидаемый URL паттерн, например, его окончание
                .andExpect(redirectedUrl("http://localhost/webui/login"));  // Либо мы можем указать точный адрес перенаправления
    }
}