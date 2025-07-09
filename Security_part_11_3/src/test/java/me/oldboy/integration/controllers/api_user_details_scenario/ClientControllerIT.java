package me.oldboy.integration.controllers.api_user_details_scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class ClientControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ClientService clientService;
    private MockMvc mockMvc;
    private ClientCreateDto testClientCreateDto, notValidClientCreateDto, existClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto, notValidClientDetailsDto;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        /* Валидные данные для сохранения при регистрации */
        testDetailsCreateDto = DetailsCreateDto.builder()
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .build();
        testClientCreateDto = ClientCreateDto.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASS)
                .details(testDetailsCreateDto)
                .build();
        existClientCreateDto = ClientCreateDto.builder()
                .email(EXIST_EMAIL)
                .password(TEST_PASS)
                .details(testDetailsCreateDto)
                .build();

        /* НЕ валидные данные для проверки валидатора при регистрации */
        notValidClientDetailsDto = DetailsCreateDto.builder()
                .clientName("A")
                .clientSurName("A")
                .age(-5)
                .build();
        notValidClientCreateDto = ClientCreateDto.builder()
                .email("er")
                .password("1")
                .details(notValidClientDetailsDto)
                .build();
    }

    @Test
    @SneakyThrows
    void registrationClient_ValidRegData_ShouldReturnOk_AndSavedClientDto_Test() {
        String requestBodyData = objectMapper.writeValueAsString(testClientCreateDto);  // Подготовим данные для сохранения
        List<ClientReadDto> beforeSaveBase = clientService.findAll();   // Получим копию списка клиентов до сохранения

        /* Имитируем запрос на сохранение клиента */
        MvcResult result = mockMvc.perform(post("/api/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isOk())
                .andReturn();

        List<ClientReadDto> afterSaveBase = clientService.findAll();    // Получим копию списка клиентов после сохранения

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        ClientReadDto savedClientDto= objectMapper.readValue(strReturn, ClientReadDto.class);

        assertAll(
                () -> assertThat(savedClientDto.getEmail()).isEqualTo(testClientCreateDto.getEmail()),
                () -> assertThat(savedClientDto.getClientName()).isEqualTo(testClientCreateDto.getDetails().clientName()),
                () -> assertThat(savedClientDto.getClientSurName()).isEqualTo(testClientCreateDto.getDetails().clientSurName()),
                () -> assertThat(savedClientDto.getAge()).isEqualTo(testClientCreateDto.getDetails().age())
        );

        assertThat(afterSaveBase.size()).isGreaterThan(beforeSaveBase.size());  // Сравним размер списка клиентов до и после сохранения
    }

    @Test
    @SneakyThrows
    void registrationClient_NotValidRegData_ShouldReturnBadRequest_AndErrorWarnings_Test() {
        String requestBodyData = objectMapper.writeValueAsString(notValidClientCreateDto);

        MvcResult result = mockMvc.perform(post("/api/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isBadRequest())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();

        assertAll(
                () -> assertThat(strReturn.contains("Standard e-mail structure - email_name@email_domain.top_lavel_domain (for example: paul@tradsystem.ru)")).isTrue(),
                () -> assertThat(strReturn.contains("Age can't be lass then 0, unless you come from a counter-directional universe!")).isTrue(),
                () -> assertThat(strReturn.contains("Password size can be between 2 and 64")).isTrue()
        );
    }

    @Test
    @SneakyThrows
    void registrationClient_TryToDuplicateReg_ShouldReturnBadRequest_AndErrorWarnings_Test() {
        String requestBodyData = objectMapper.writeValueAsString(existClientCreateDto);

        MvcResult result = mockMvc.perform(post("/api/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isBadRequest())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();

        assertThat(strReturn.contains("Email: " + EXIST_EMAIL + " is exist, can not duplicate data.")).isTrue();
    }
}