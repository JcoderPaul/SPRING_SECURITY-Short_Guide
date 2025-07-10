package me.oldboy.integration.controllers.api_jwt_scenario;

import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL_WITH_READ_AUTH;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class JwtAccountControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    /* В данном тесте для его прохождения нам нужен только claim - "sub", остальные "антураж" (можно удалить) */
    @Test
    @SneakyThrows
    void getAccountDetails_ShouldReturnOk_AndAccountRecord_CorrectJwt_Test() {
        mockMvc.perform(get("/api/myAccount")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("sub", EXIST_EMAIL_WITH_READ_AUTH)   // Существенный для теста claim
                                .claim("preferred_username", EXIST_EMAIL_WITH_READ_AUTH)    // НЕ существенный для теста claim
                                .claim("scope", "openid profile"))  // НЕ существенный для теста claim
                                .authorities(new SimpleGrantedAuthority("ROLE_READ")))) // НЕ существенный для теста claim
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("accountNumber")))
                .andExpect(content().string(containsString("accountType")))
                .andExpect(content().string(containsString("branchAddress")))
                .andExpect(content().string(containsString("createDt")));
    }

    @Test
    @SneakyThrows
    void getAccountDetails_ShouldReturn_401_WithOutJwt_Test() {
        mockMvc.perform(get("/api/myAccount")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }
}