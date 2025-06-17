package me.oldboy.unit.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.TestWebAppInitializer;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.api.CardsController;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.services.CardService;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestWebAppInitializer.class})
@WebAppConfiguration
class CardsControllerTest {

    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private CardService cardService;
    @MockitoBean
    private UserDetailsDetector userDetailsDetector;
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;

    @InjectMocks
    private CardsController cardsController;

    private MockMvc mockMvc;

    private Client testClient;
    private CardReadDto testCardReadDto;
    private List<CardReadDto> testList;
    private Long testId;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(cardsController).build();

        testId = 1L;

        testClient = Client.builder()
                .id(testId)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .build();

        testCardReadDto = CardReadDto.builder()
                .createDt(LocalDate.of(2011, 04,12))
                .availableAmount(12000)
                .amountUsed(3450)
                .totalLimit(15000)
                .cardType("Credit")
                .cardNumber("2123XXXX4355")
                .build();

        mockClientDetails = new SecurityClientDetails(testClient);
        mockAuthentication = new TestingAuthenticationToken(mockClientDetails, TEST_PASS, mockClientDetails.getAuthorities());

        testList = List.of(testCardReadDto);
    }

    @Test
    @SneakyThrows
    void shouldReturn_2XX_AndResponseList_GetCardDetails_Test() {
        /* "Заглушаем" систему безопасности */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* "Заглушаем" ключевую логику метода */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(true);
        when(userDetailsDetector.getClientId()).thenReturn(testId);
        when(cardService.findAllCardsByClientId(testId)).thenReturn(testList);

        /* Имитируем запрос и ожидаемый ответ */
        MvcResult mvcResult = mockMvc.perform(get("/api/myCards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        /* Получаем результат запроса -> ответ в виде строки */
        String strResult = mvcResult.getResponse().getContentAsString();

        /* Парсим результат response, ранее полученный как строка, в массив CardReadDto объектов */
        CardReadDto[] cardArrayFromResponse = objectMapper.readValue(strResult, CardReadDto[].class);

        /* Сличаем исходный CardReadDto и возвращенный из response после "прогона" тестируемого метода */
        assertThat(testList.get(0)).isEqualTo(cardArrayFromResponse[0]);
    }

    /*
        Теоретически можно представить, что мы прошли этап аутентификации, но в ходе
        работы текущего метода либо сервис, либо объект аутентификации были повреждены.
    */
    @Test
    @SneakyThrows
    void shouldReturn_2XX_AndEmptyResponse_GetCardDetails_Test() {
        /* "Заглушаем" систему безопасности */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* "Заглушаем" ключевую логику метода - имитируем проблемы аутентификации */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(false);

        /* Имитируем запрос */
        mockMvc.perform(get("/api/myCards"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }
}