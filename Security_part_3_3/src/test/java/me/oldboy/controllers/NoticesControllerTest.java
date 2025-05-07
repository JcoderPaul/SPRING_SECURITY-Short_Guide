package me.oldboy.controllers;

import me.oldboy.config.AppSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppSecurityConfig.class})
@WebAppConfiguration
class NoticesControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setApplicationContext(){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturn_200_WithAnyAuthUser_GetNoticesTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notices"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("Here are the notices details from the DB");
    }
}