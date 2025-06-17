package me.oldboy.unit.controllers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.api.LoansController;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.services.ClientService;
import me.oldboy.services.LoanService;
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
@WebAppConfiguration
@ContextConfiguration(classes = {LoansController.class})
class LoansControllerTest {

    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private LoanService loanService;
    @MockitoBean
    private UserDetailsDetector userDetailsDetector;
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;
    @InjectMocks
    private LoansController loansController;
    private MockMvc mockMvc;

    private Client testClient;
    private List<LoanReadDto> testList;
    private LoanReadDto testLoanReadDto;
    private Long testId;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(loansController).build();

        testId = 1L;

        testClient = Client.builder()
                .id(testId)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .build();

        testLoanReadDto = LoanReadDto.builder()
                .createDate(LocalDate.of(2011, 06,12))
                .outstandingAmount(30000)
                .amountPaid(40000)
                .totalLoan(40000)
                .loanType("GoodPlace")
                .startDate(LocalDate.of(2011, 06, 12))
                .build();

        mockClientDetails = new SecurityClientDetails(testClient);
        mockAuthentication = new TestingAuthenticationToken(mockClientDetails, TEST_PASS, mockClientDetails.getAuthorities());

        testList = List.of(testLoanReadDto);
    }

    @Test
    @SneakyThrows
    void shouldReturnOk_AndDtoList_GetLoanDetails_Test() {
        /* "Заглушаем" систему безопасности */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* "Заглушаем" ключевую логику метода */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(true);
        when(userDetailsDetector.getClientId()).thenReturn(testId);
        when(loanService.readAllLoansByUserId(testId)).thenReturn(testList);

        /* Имитируем запрос и ожидаемый ответ */
        MvcResult mvcResult = mockMvc.perform(get("/api/myLoans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        /* Получаем результат запроса -> ответ в виде строки */
        String strResult = mvcResult.getResponse().getContentAsString();

        /* Парсим результат response, ранее полученный как строка, в массив CardReadDto объектов */
        List<LoanReadDto> loanListFromResponse = objectMapper.readValue(strResult, new TypeReference<List<LoanReadDto>>() {});

        /* Сличаем исходный CardReadDto и возвращенный из response после "прогона" тестируемого метода */
        assertThat(testList.get(0)).isEqualTo(loanListFromResponse.get(0));
    }

    @Test
    @SneakyThrows
    void shouldReturn_2XX_AndEmptyPage_GetLoanDetails_Test() {
        /* "Заглушаем" систему безопасности */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* "Заглушаем" ключевую логику метода */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(false);

        /* Имитируем запрос и ожидаемый ответ */
        mockMvc.perform(get("/api/myLoans"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }
}