package me.oldboy.controllers.api;

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
    В этот раз в отличие от предыдущих, мы имеем в цепочке безопасности больше настроек
    и самое важно, что при обращении к защищенному endpoint-у, без аутентификации нас
    перебросит на страницу логина.
*/
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    @WithMockUser // Имитируем аутентифицированного пользователя
    void shouldReturnOkWithAuthClientGetAccountDetailsTest(){
        mockMvc.perform(get("/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the account details from the DB"));
    }

    /*
        Раньше при отсутствии аутентификации мы получали ответ из списка 4хх,
        теперь, как описано выше, будет перенаправление запроса или 3хх ответ.
    */
    @Test
    @SneakyThrows
    void shouldReturnUnAuthWithoutAuthClientGetAccountDetailsTest(){
        mockMvc.perform(get("/myAccount"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""))
                .andExpect(redirectedUrlPattern("**/login"));
    }
}