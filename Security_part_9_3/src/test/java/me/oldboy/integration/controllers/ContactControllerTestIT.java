package me.oldboy.integration.controllers;

import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class ContactControllerTestIT extends TestContainerInit {

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
    void getContactDetails_ShouldReturnContactReadDto_Test() {
        MvcResult result = mockMvc.perform(get("/api/myContact"))
                .andExpect(status().isOk())
                .andReturn();

        String strResponse = result.getResponse().getContentAsString();
        /* Проверяем наличие DTO полей в ответе (он явно не пустой) */
        assertAll(
                () -> assertThat(strResponse.contains("city")).isTrue(),
                () -> assertThat(strResponse.contains("postalCode")).isTrue(),
                () -> assertThat(strResponse.contains("address")).isTrue(),
                () -> assertThat(strResponse.contains("building")).isTrue(),
                () -> assertThat(strResponse.contains("apartment")).isTrue(),
                () -> assertThat(strResponse.contains("homePhone")).isTrue(),
                () -> assertThat(strResponse.contains("mobilePhone")).isTrue()
        );
    }

    @Test
    @SneakyThrows
    void getContactDetails_ShouldReturnForbidden_Test() {
        mockMvc.perform(get("/api/myContact"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}