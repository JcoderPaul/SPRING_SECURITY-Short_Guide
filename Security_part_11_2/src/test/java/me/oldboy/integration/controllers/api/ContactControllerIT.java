package me.oldboy.integration.controllers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.dto.contact_dto.ContactReadDto;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ContactControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
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

    /* Ниже два теста проверяют работу @PreAuthorize("hasRole('ADMIN')") над методом, ну и сам метод в случае успеха */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getAllContacts_ShouldReturnDtoList_AdminAuth_Test() {
        MvcResult result = mockMvc.perform(get("/api/user-contacts"))
                .andExpect(status().isOk())
                .andReturn();

        String strResultResponse = result.getResponse().getContentAsString();

        List<ContactReadDto> listFromResponse = new ObjectMapper()
                .readValue(strResultResponse, new TypeReference<List<ContactReadDto>>() {});

        assertThat(listFromResponse.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL_WITH_READ_AUTH, userDetailsServiceBeanName = "clientDetailsService")
    void getAllContacts_ShouldReturnBadRequest_NotAdminAuth_Test() {
        mockMvc.perform(get("/api/user-contacts"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Access Denied\"}"));
    }

    /* Ниже тесты проверяют работу @PostAuthorize("#myCity == returnObject.city") над методом, ну и сам метод в случае успеха */
    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getContactsIfConditionIsGood_ShouldReturnOneRecordOfExistingCity_Test() {
        String testCity = "Lovervill";

        MvcResult result = mockMvc.perform(get("/api/user-contact-with-condition/" + testCity))
                .andExpect(status().isOk())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();

        ContactReadDto respDto = new ObjectMapper().readValue(strRes, ContactReadDto.class);

        assertThat(respDto.getCity()).isEqualTo(testCity);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getContactsIfConditionIsGood_ShouldReturnBadRequest_NotExistingCity_Test() {
        String testCity = "Ufa";

        mockMvc.perform(get("/api/user-contact-with-condition/" + testCity))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Access Denied\"}"));
    }
}