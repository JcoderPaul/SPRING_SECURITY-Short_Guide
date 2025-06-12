package me.oldboy.integration.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.money.Loan;
import me.oldboy.repository.LoanRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class LoanRepositoryTestIT extends IntegrationTestBase {

    private final LoanRepository loanRepository;

    private Long clientId;

    @Test
    @DisplayName("Test 9: Find all loans by client ID from DB")
    @Order(9)
    @Rollback(value = false)
    void shouldReturnTrue_LoanList_If_FindAllByClientId_Test() {
        clientId = 1L;
        Optional<List<Loan>> mayBeLoans = loanRepository.findAllByClientId(clientId);

        assertThat(mayBeLoans.isPresent()).isTrue();
        assertThat(mayBeLoans.get().size() > 0).isTrue();
    }

    @Test
    @DisplayName("Test 10: Can not find loans by not existing client ID")
    @Order(10)
    @Rollback(value = false)
    void shouldReturnEmptyList_If_NotFindAllLoansByClientId_Test() {
        clientId = 10L;
        Optional<List<Loan>> mayBeLoans = loanRepository.findAllByClientId(clientId);

        assertThat(mayBeLoans.get().size()).isEqualTo(0);
    }
}