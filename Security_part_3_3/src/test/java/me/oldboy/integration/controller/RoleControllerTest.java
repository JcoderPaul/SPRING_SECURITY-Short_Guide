package me.oldboy.integration.controller;

import lombok.SneakyThrows;
import me.oldboy.config.test_context.TestDataSourceConfig;
import me.oldboy.controllers.RoleController;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.PrintAuthorities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@IT
@ContextConfiguration(classes = {TestDataSourceConfig.class})
@ExtendWith(SpringExtension.class)
class RoleControllerTest {

    @Autowired
    private RoleController roleController;

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
}