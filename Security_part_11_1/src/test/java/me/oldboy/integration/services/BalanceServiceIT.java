package me.oldboy.integration.services;

import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.services.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceServiceIT extends IntegrationTestBase {

    @Autowired
    private BalanceService balanceService;

    private Long clientId;

    @Test
    void readAllTransactionByClientId_ShouldReturnTransactionList_Test() {
        clientId = 1L;
        List<TransactionReadDto> allTransaction = balanceService.readAllTransactionByClientId(clientId);

        assertThat(allTransaction.size() > 0).isTrue();
    }

    @Test
    void readAllTransactionByClientId_ShouldReturnEmptyList_NotExistClientId_Test() {
        clientId = 10L;
        List<TransactionReadDto> allTransaction = balanceService.readAllTransactionByClientId(clientId);

        assertThat(allTransaction.size()).isEqualTo(0);
    }
}