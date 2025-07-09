package me.oldboy.integration.services;

import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.loan_dto.LoanCreateDto;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.exception.LoanServiceException;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IT
class LoanServiceIT extends TestContainerInit {

    @Autowired
    private LoanService loanService;

    private Long testExistId, testNotExistId;
    private LoanCreateDto testExistingClientLoanCreateDto, anotherExistingClientLoanCreateDto, testNonExistingClientLoanCreateDto;
    private Client testClient;
    private SecurityClientDetails testSecurityClientDetails, emptySecurityClientDetails;
    private List<LoanCreateDto> testLoanCreateDtoList;
    private String loanTypeOne, loanTypeTwo;

    @BeforeEach
    void setUp(){
        testExistId = 1L;
        testNotExistId = 100L;

        loanTypeOne = "Yacht";
        loanTypeTwo = "Wine collection";

        testExistingClientLoanCreateDto = LoanCreateDto.builder()
                .clientId(testExistId)
                .startDate(LocalDate.of(2018, 06,12))
                .loanType(loanTypeOne)
                .totalLoan(120000)
                .amountPaid(12000)
                .outstandingAmount(80000)
                .createDate(LocalDate.of(2018, 06,12))
                .build();

        testNonExistingClientLoanCreateDto = LoanCreateDto.builder()
                .clientId(testNotExistId)
                .build();

        testClient = Client.builder()
                .id(testExistId)
                .role(Role.ROLE_ADMIN)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .build();

        testSecurityClientDetails = new SecurityClientDetails(testClient);
        emptySecurityClientDetails = new SecurityClientDetails();

        anotherExistingClientLoanCreateDto = LoanCreateDto.builder()
                .clientId(testExistId)
                .startDate(LocalDate.of(2010, 11,22))
                .loanType(loanTypeTwo)
                .totalLoan(120000)
                .amountPaid(12000)
                .outstandingAmount(80000)
                .createDate(LocalDate.of(2010, 11,22))
                .build();
        testLoanCreateDtoList = List.of(testExistingClientLoanCreateDto, anotherExistingClientLoanCreateDto);
    }

    @Test
    void readAllLoansByUserId_ShouldReturnList_IfClientHasLoans_Test() {
        List<LoanReadDto> listFromBase = loanService.readAllLoansByUserId(testExistId);
        assertThat(listFromBase.size()).isGreaterThan(0);
    }

    @Test
    void readAllLoansByUserId_ShouldReturnEmptyList_IfClientHasNoLoans_Test() {
        List<LoanReadDto> listFromBase = loanService.readAllLoansByUserId(testNotExistId);
        assertThat(listFromBase.size()).isEqualTo(0);
    }

    @Test
    void saveLoan_ShouldReturnSavedLoanId_ExistingClientId_Test() {
        Long mayBeSaveLoanId = loanService.saveLoan(testExistingClientLoanCreateDto);
        assertThat(mayBeSaveLoanId).isNotNull();
    }

    @Test
    void saveLoan_ShouldReturnException_NotExistingClientId_Test() {
        assertThatThrownBy(() -> loanService.saveLoan(testNonExistingClientLoanCreateDto))
                .isInstanceOf(LoanServiceException.class)
                .hasMessageContaining("Can not find clientId = " + testNonExistingClientLoanCreateDto.getClientId());
    }

    @Test
    void saveAllMyLoans_ShouldReturnTrue_AndIncreaseLoanListSize_Test() {
        List<LoanReadDto> listBeforeSave = loanService.readAllLoansByUserId(testExistId);

        /* Проверяем что сохранил */
        boolean isLoanListSaved = loanService.saveAllMyLoans(testLoanCreateDtoList);
        assertThat(isLoanListSaved).isTrue();

        /* Проверяем что число кредитов стало больше */
        List<LoanReadDto> listAfterSave = loanService.readAllLoansByUserId(testExistId);
        assertThat(listAfterSave.size()).isGreaterThan(listBeforeSave.size());
    }

    /* Проверяем, что БД в принципе содержит записи о кредитах, затем проверяем на точный размер известный нам 8 */
    @Test
    void findAll_ShouldReturnListSize_Test() {
        List<LoanReadDto> getAllLoansFromBase = loanService.findAll();
        assertThat(getAllLoansFromBase.size()).isGreaterThan(0);    // Список не пустой
        assertThat(getAllLoansFromBase.size()).isEqualTo(8);    // Список равен известному размеру
    }
}