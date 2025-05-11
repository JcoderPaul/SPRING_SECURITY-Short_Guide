package me.oldboy.integration.controller;

import lombok.SneakyThrows;
import me.oldboy.controllers.RoleController;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.PrintAuthorities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IT
@AutoConfigureMockMvc
class RoleControllerTestIT {

    @Autowired
    private RoleController roleController;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnActualSizeOfTableRecordListGetClientListTest() {
        List<PrintAuthorities> printAuthoritiesList = roleController.getClientList();
        assertThat(printAuthoritiesList.size()).isEqualTo(4);
    }

    @Test
    @WithMockUser(roles = "HR")
    @SneakyThrows
    void shouldReturnOkAndJsonListGetClientList() {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/roleList"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("[{\"username\":\"Paul\",\"authority\":\"ROLE_EMPLOYEE\"}," +
                        "{\"username\":\"Sasha\",\"authority\":\"ROLE_HR\"}," +
                        "{\"username\":\"Stasya\",\"authority\":\"ROLE_HR\"}," +
                        "{\"username\":\"Stasya\",\"authority\":\"ROLE_MANAGER\"}]");
    }

    @Test
    @SneakyThrows
    void shouldReturn_4xx_WithOutAuthUser_GetClientList() {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/roleList"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("");
    }
}