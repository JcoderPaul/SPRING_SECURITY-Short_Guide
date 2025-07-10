package me.oldboy.integration.controllers.api_jwt_scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL_WITH_READ_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class JwtBalanceControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    /* Фактически для тестирования двух следующих тестов достаточно в токен передать claim - "sub", тут нет проверки ROLE и т.п. */
    @Test
    @SneakyThrows
    void getBalanceDetails_ShouldReturnOk_AndDtoList_Test() {
        MvcResult result = mockMvc.perform(get("/api/myBalance")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("sub", EXIST_EMAIL)))) // У клиента с EXIST_EMAIL есть записи в БД
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
    void getBalanceDetails_ShouldReturn_2xx_NoContent_ClientHasNoTransaction_Test() {
        MvcResult result = mockMvc.perform(get("/api/myBalance")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("sub", EXIST_EMAIL_WITH_READ_AUTH))))  // У клиента с EXIST_EMAIL_WITH_READ_AUTH нет записей в БД
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