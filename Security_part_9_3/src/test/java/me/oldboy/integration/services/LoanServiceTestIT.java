package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class LoanServiceTestIT extends TestContainerInit {

    @Autowired
    private LoanService loanService;

    private Long testExistId, testNotExistId;

    @BeforeEach
    void setUp(){
        testExistId = 1L;
        testNotExistId = 100L;
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
}