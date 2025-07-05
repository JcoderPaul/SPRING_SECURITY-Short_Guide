package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AccountControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getAccountDetails_ShouldReturnOk_Test() {
        MvcResult result = mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();

        AccountReadDto respDto = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strRes, AccountReadDto.class);

        assertAll(
                () -> assertThat(respDto).isNotNull(),
                () -> assertThat(respDto.getBranchAddress()).isEqualTo("123 Main Street, New York")
        );
    }
}