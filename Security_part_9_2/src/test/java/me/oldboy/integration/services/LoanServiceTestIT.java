package me.oldboy.integration.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.integration.IntegrationTestBaseConnection;
import me.oldboy.services.LoanService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class LoanServiceTestIT extends IntegrationTestBaseConnection {

    private final LoanService loanService;

    private Long clientId;

    @Test
    void shouldReturnTrue_LoanReadDtoList_ReadAllLoansByUserId_Test() {
        clientId = 1L;
        List<LoanReadDto> allClientLoans = loanService.readAllLoansByUserId(clientId);

        assertThat(allClientLoans.size() > 0).isTrue();
    }

    @Test
    void shouldReturnEmptyList_NotExistClient_ReadAllLoansByUserId_Test() {
        clientId = 10L;
        List<LoanReadDto> allClientLoans = loanService.readAllLoansByUserId(clientId);

        assertThat(allClientLoans.size()).isEqualTo(0);
    }
}