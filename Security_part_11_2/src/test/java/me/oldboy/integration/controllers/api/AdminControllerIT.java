package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.dto.client_dto.ClientReadDto;
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
class AdminControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getAllClient_ShouldReturnOk_AndClientList_WithAdminAuth_Test() {
        MvcResult result = mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();
        List<ClientReadDto> clientListFromBase = new ObjectMapper()
                .readValue(strRes, new TypeReference<List<ClientReadDto>>() {});

        assertThat(clientListFromBase.size()).isGreaterThan(3); // В базе точно есть записи и их более 3-х
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL_WITH_READ_AUTH, userDetailsServiceBeanName = "clientDetailsService")
    void getAllClient_ShouldReturnForbidden_WithoutAdminAuth_Test() {
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}