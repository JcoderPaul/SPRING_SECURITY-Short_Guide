package me.oldboy.integration.controllers.api_user_details_scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.loan_dto.LoanCreateDto;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class LoansControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private LoanService loanService;
    private MockMvc mockMvc;

    private Long testId, anotherId;
    private LoanCreateDto testLoanCreateDtoForOwner, testLoanCreateDtoToAnotherClient;
    private List<LoanCreateDto> testList;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        testId = 1L;
        anotherId = 3L;

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        testLoanCreateDtoForOwner = LoanCreateDto.builder()
                .clientId(testId)
                .startDate(LocalDate.of(2021, 04,05))
                .loanType("Plane")
                .totalLoan(350000)
                .amountPaid(65000)
                .outstandingAmount(210000)
                .createDate(LocalDate.of(2021, 03,05))
                .build();
        testLoanCreateDtoToAnotherClient = LoanCreateDto.builder()
                .clientId(anotherId)
                .startDate(LocalDate.of(2022, 11,12))
                .loanType("Castle")
                .totalLoan(1350000)
                .amountPaid(115000)
                .outstandingAmount(600000)
                .createDate(LocalDate.of(2022, 10,03))
                .build();

        testList = List.of(testLoanCreateDtoForOwner, testLoanCreateDtoToAnotherClient);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
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
    @WithUserDetails(value = "user@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void getLoanDetails_ShouldReturnEmptyBody_IfClientHasNoLoans_Test() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/myLoans"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void getLoanDetails_ShouldReturnNotAuth_4xx_WithoutAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/myLoans"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void createLoan_ShouldReturnOk_AndRecordIdFromBase_Test() {
        /* Подготовим данные для сохранения в БД*/
        String strLoanCreateDto = objectMapper.writeValueAsString(testLoanCreateDtoForOwner);

        /* Делаем запрос на сохранение */
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/createLoan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strLoanCreateDto))
                .andExpect(status().isOk())
                .andReturn();

        /* "Парсим" ответ */
        String strRes = result.getResponse().getContentAsString();
        Long afterCreateLoanId = objectMapper.readValue(strRes, Long.class);

        /* Сравниваем ожидание с результатом - ID последней записи будет больше последнего известного из БД - 8 */
        assertThat(afterCreateLoanId).isGreaterThan(8);
    }

    /* Id аутентифицированного клиента и Id того на кого оформлен кредит не совпадает - сохранить нельзя */

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void createLoan_ShouldReturnForbidden_TryToSaveNotYoursLoan_Test() {
        /* Подготовим данные для сохранения в БД*/
        String strLoanCreateDto = objectMapper.writeValueAsString(testLoanCreateDtoToAnotherClient);

        /* Делаем запрос на сохранение */
        mockMvc.perform(MockMvcRequestBuilders.post("/api/createLoan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strLoanCreateDto))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void saveAllMyRequestLoan_ShouldReturnOk_AndSaveOnlyAuthOwnerLoans_Test() {
        /* Получим количество записей в БД */
        List<LoanReadDto> loansList = loanService.findAll();
        Integer listSizeBefore = loansList.size();

        /* Подготовим данные для сохранения в БД */
        String strList = objectMapper.writeValueAsString(testList);

        /* Делаем запрос на сохранение */
        mockMvc.perform(MockMvcRequestBuilders.post("/api/save-all-loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strList))
                .andExpect(status().isOk())
                .andExpect(content().string("Saved all loans!"));

        /* Проверяем количество записей в БД после сохранения списка кредитов - больше на одну */
        Integer listSizeAfter = loanService.findAll().size();
        assertThat(listSizeAfter).isEqualTo(listSizeBefore + 1);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "user3@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void saveAllMyRequestLoan_ShouldReturnOk_ButOperationFailed_Test() {
        /* Получим количество записей в БД */
        List<LoanReadDto> loansList = loanService.findAll();
        Integer listSizeBefore = loansList.size();

        /* Подготовим данные для сохранения в БД */
        String strList = objectMapper.writeValueAsString(testList);

        /* Делаем запрос на сохранение */
        mockMvc.perform(MockMvcRequestBuilders.post("/api/save-all-loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strList))
                .andExpect(status().isOk())
                .andExpect(content().string("Operation is failed!"));

        /* Проверяем количество записей в БД после сохранения списка кредитов - неизменен */
        Integer listSizeAfter = loanService.findAll().size();
        assertThat(listSizeAfter).isEqualTo(listSizeBefore);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void getAllLoanByType_ShouldReturnOkForRoleAdmin_AndListOfLoansByType_Test() {
        String loanType = "Home";

        MvcResult result = mockMvc.perform(get("/api/get-all-loans-by-type/" + loanType))
                .andExpect(status().isOk())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();

        List<LoanReadDto> respList = objectMapper.readValue(strRes, new TypeReference<List<LoanReadDto>>() {});

        assertThat(respList.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "user3@test.com", userDetailsServiceBeanName = "clientDetailsService")
    void getAllLoanByType_ShouldReturnForbidden_NotAdminAuth_Test() {
        String loanType = "Home";

        mockMvc.perform(get("/api/get-all-loans-by-type/" + loanType))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Access Denied\"}"));
    }
}