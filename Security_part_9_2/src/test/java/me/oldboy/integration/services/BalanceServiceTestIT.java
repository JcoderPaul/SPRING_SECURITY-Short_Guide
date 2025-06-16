package me.oldboy.integration.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.integration.IntegrationTestBaseConnection;
import me.oldboy.services.BalanceService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class BalanceServiceTestIT extends IntegrationTestBaseConnection {

    private final BalanceService balanceService;

    private Long clientId;

    @Test
    void shouldReturnTransactionList_ReadAllTransactionByClientId_Test() {
        clientId = 1L;
        List<TransactionReadDto> allTransaction = balanceService.readAllTransactionByClientId(clientId);

        assertThat(allTransaction.size() > 0).isTrue();
    }

    @Test
    void shouldReturnEmptyList_NotExistClientId_ReadAllTransactionByClientId_Test() {
        clientId = 10L;
        List<TransactionReadDto> allTransaction = balanceService.readAllTransactionByClientId(clientId);

        assertThat(allTransaction.size()).isEqualTo(0);
    }
}