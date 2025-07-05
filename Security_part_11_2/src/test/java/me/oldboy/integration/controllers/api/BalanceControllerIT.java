package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL_WITH_READ_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class BalanceControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getBalanceDetails_ShouldReturnOk_Test() {
        MvcResult result = mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();

        List<TransactionReadDto> respDtoList = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strRes, new TypeReference<List<TransactionReadDto>>() {});

        assertThat(respDtoList.size()).isGreaterThan(3);    // У данного клиента более 3-х транзакций в БД
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL_WITH_READ_AUTH, userDetailsServiceBeanName = "clientDetailsService")
    void getBalanceDetails_ShouldReturn_204_NoContent_ClientHasNoTransaction_Test() {
        mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));   // У данного клиента нет транзакций в БД
    }
}