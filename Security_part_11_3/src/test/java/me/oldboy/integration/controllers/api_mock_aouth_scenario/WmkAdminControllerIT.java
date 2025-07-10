package me.oldboy.integration.controllers.api_mock_aouth_scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.client_dto.ClientReadDto;
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
class WmkAdminControllerIT extends TestContainerInit {

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
    void getAllClient_ShouldReturnDtoList_AuthAdmin_Test() {
        MvcResult result = mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();

        List<ClientReadDto> listFromResponse =
                new ObjectMapper().readValue(strResult, new TypeReference<List<ClientReadDto>>() {});

        assertThat(listFromResponse.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    @WithMockOAuth2User(username = "user3@test.com", email = "user3@test.com", authorities = {"ROLE_USER"})
    void getAllClient_ShouldReturnForbidden_AuthNotAdminOrRead_Test() {
        mockMvc.perform(get("/api/admin/getAllClient"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}