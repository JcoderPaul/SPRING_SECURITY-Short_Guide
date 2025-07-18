package me.oldboy.integration.controllers.api;

import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class AccountControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS)
    void getAccountDetails_ShouldReturnOk_AndAccountRecord_ForCurrentClient_Test() {
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accountNumber")))
                .andExpect(content().string(containsString("accountType")))
                .andExpect(content().string(containsString("branchAddress")))
                .andExpect(content().string(containsString("createDt")));
    }

    @Test
    @SneakyThrows
    void getAccountDetails_ShouldReturn_401_NotAuthClient_Test() {
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }
}