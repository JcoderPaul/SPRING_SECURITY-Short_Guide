package me.oldboy.integration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class LoansControllerTestIT extends TestContainerInit {

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
    void getLoanDetails_ShouldReturnDtoList_ForClientWithLoans_Test() {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/myLoans"))
                .andExpect(status().isOk())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();
        List<LoanReadDto> loansListFromBase = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strRes, new TypeReference<List<LoanReadDto>>() {});

        assertThat(loansListFromBase.size()).isGreaterThan(0);
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user@test.com", password = TEST_PASS)
    void getLoanDetails_ShouldReturnEmptyBody_IfClientHasNoLoans_Test() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/myLoans"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void getLoanDetails_ShouldReturnForbidden_WithoutAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/myLoans"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}