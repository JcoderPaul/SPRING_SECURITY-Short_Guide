package me.oldboy.integration.controllers.api_jwt_scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class JwtAdminControllerIT extends TestContainerInit {

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
    void getAllClient_ShouldReturnDtoList_JwtClaimWithAdmin_Test() {
        MvcResult result = mockMvc.perform(get("/api/admin/getAllClient")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("preferred_username", EXIST_EMAIL)
                                .claim("scope", "openid profile"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))    // Важный для прохождения теста метод
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String strResult = result.getResponse().getContentAsString();

        List<ClientReadDto> listFromResponse =
                new ObjectMapper().readValue(strResult, new TypeReference<List<ClientReadDto>>() {});

        assertThat(listFromResponse.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    void getAllClient_ShouldReturnForbidden_JwtClaimWithUser_Test() {
        mockMvc.perform(get("/api/admin/getAllClient")
                        .with(jwt().jwt(builder -> builder
                                        .claim("scope", "openid profile")
                                        .claim("preferred_username", "user3@test.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}