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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppSecurityConfig.class})
@WebAppConfiguration
class CardsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setWebApplicationContext(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = "READ")
    void shouldReturnOkWithAuthUserGetCardDetailsTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/myCards"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("Here are the card details from the DB");
    }

    @Test
    void shouldReturn_4xx_WithoutAuthUser_GetCardDetailsTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/myCards"))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("");
    }
}