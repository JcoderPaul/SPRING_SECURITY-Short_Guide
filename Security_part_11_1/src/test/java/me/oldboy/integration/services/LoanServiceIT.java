package me.oldboy.integration.services;

import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.services.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanServiceIT extends IntegrationTestBase {

    @Autowired
    private LoanService loanService;

    private Long clientId;

    @Test
    void readAllLoansByUserId_ShouldReturnTrue_AndLoanReadDtoList_Test() {
        clientId = 1L;
        List<LoanReadDto> allClientLoans = loanService.readAllLoansByUserId(clientId);

        assertThat(allClientLoans.size() > 0).isTrue();
    }

    @Test
    void readAllLoansByUserId_ShouldReturnEmptyList_ForNotExistClient_Test() {
        clientId = 10L;
        List<LoanReadDto> allClientLoans = loanService.readAllLoansByUserId(clientId);

        assertThat(allClientLoans.size()).isEqualTo(0);
    }
}