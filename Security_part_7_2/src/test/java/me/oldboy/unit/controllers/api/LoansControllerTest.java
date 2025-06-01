package me.oldboy.unit.controllers.api;

import me.oldboy.config.security_config.AppSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppSecurityConfig.class})
@WebAppConfiguration
class LoansControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setAppContext(){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void shouldReturn_200_WithAuthUser_GetLoanDetailsTest() throws Exception {
        mockMvc.perform(get("/api/myLoans"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Here are the loan details from the DB"));
    }

    @Test
    void shouldReturn_401_WithoutAuthUser_GetLoanDetailsTest() throws Exception {
        mockMvc.perform(get("/api/myLoans"))
                .andExpect(status().is4xxClientError());
    }
}