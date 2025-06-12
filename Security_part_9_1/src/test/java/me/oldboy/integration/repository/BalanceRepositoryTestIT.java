package me.oldboy.integration.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.money.Transaction;
import me.oldboy.repository.BalanceRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class BalanceRepositoryTestIT extends IntegrationTestBase {

    private final BalanceRepository balanceRepository;

    private Long clientId, accountNumber;

    @Test
    @DisplayName("Test 3: Return transaction list by existing client ID from DB")
    @Order(3)
    @Rollback(value = false)
    void shouldReturnTransactionList_IfFindItByClientId_Test() {
        clientId = 1L;
        Optional<List<Transaction>> mayBeList = balanceRepository.findByClientId(clientId);

        assertThat(mayBeList.isPresent()).isTrue();
        assertThat(mayBeList.get().size() > 0).isTrue();
    }

    @Test
    @DisplayName("Test 4: Return empty list by not existing client ID")
    @Order(4)
    @Rollback(value = false)
    void shouldReturnZeroSizeList_IfNotFindByClientId_Test() {
        clientId = 10L;
        Optional<List<Transaction>> mayBeList = balanceRepository.findByClientId(clientId);

        assertThat(mayBeList.get().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Test 5: Return transaction list by existing account number from DB")
    @Order(5)
    @Rollback(value = false)
    void shouldReturnTransactionList_IfFindByAccountNumber_Test() {
        accountNumber = 186576453434L;
        Optional<List<Transaction>> mayBeList = balanceRepository.findByAccountNumber(accountNumber);

        assertThat(mayBeList.isPresent()).isTrue();
        assertThat(mayBeList.get().size() > 0).isTrue();
    }

    @Test
    @DisplayName("Test 6: Return empty transaction list by not existing account number")
    @Order(6)
    @Rollback(value = false)
    void shouldReturnEmptyList_IfFindByAccountNumber_Test() {
        accountNumber = 9327637L;
        Optional<List<Transaction>> mayBeList = balanceRepository.findByAccountNumber(accountNumber);

        assertThat(mayBeList.get().size()).isEqualTo(0);
    }
}