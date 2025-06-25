package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.controllers.api.CardsController;
import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class CardsControllerIT extends TestContainerInit {

    @Autowired
    private CardsController cardsController;
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
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getCardDetails_ShouldReturnDtoList_AndOkAdminAuth_Test() {
        MvcResult result = mockMvc.perform(get("/api/myCards"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<CardReadDto> listFromResponse = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strResult, new TypeReference<List<CardReadDto>>() {});

        assertThat(listFromResponse.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "user@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void getCardDetails_ShouldReturnDtoList_AndOkReadAuth_Test() {
        MvcResult result = mockMvc.perform(get("/api/myCards"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<CardReadDto> listFromResponse = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strResult, new TypeReference<List<CardReadDto>>() {});

        assertThat(listFromResponse.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "user3@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void getCardDetails_ShouldReturnForbidden_NotReadOrAdminAuth_Test() {
        mockMvc.perform(get("/api/myCards"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}