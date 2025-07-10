package me.oldboy.integration.controllers.api_mock_aouth_scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.controllers.api.BalanceController;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.integration.annotation.WithMockOAuth2User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class WmkBalanceControllerIT extends TestContainerInit {

    @Autowired
    private BalanceController balanceController;
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
    @WithMockOAuth2User(username = EXIST_EMAIL, email = EXIST_EMAIL, authorities = {"ROLE_ADMIN"})
    void getBalanceDetails_ShouldReturnOk_AndDtoList_Test() {
        MvcResult result = mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<TransactionReadDto> listFromResponse = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strResult, new TypeReference<List<TransactionReadDto>>() {});

        assertThat(listFromResponse.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    @WithMockOAuth2User(username = "user@test.com", email = "user@test.com", authorities = {"ROLE_ADMIN"})
    void getBalanceDetails_ShouldReturn_2xx_NoContent_ClientHasNoTransaction_Test() {
        MvcResult result = mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();

        assertThat(strResult.isEmpty()).isTrue();
    }

    @Test
    @SneakyThrows
    void getBalanceDetails_ShouldReturnNotAuth_4xx_Test() {
        mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }
}