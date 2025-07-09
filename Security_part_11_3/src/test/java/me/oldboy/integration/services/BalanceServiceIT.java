package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class BalanceServiceIT extends TestContainerInit {

    @Autowired
    private BalanceService balanceService;

    private Long testExistId, testNotExistId;

    @BeforeEach
    void setUp(){
        testExistId = 1L;
        testNotExistId = 100L;
    }

    @Test
    void readAllTransactionByClientId_ShouldReturnNotEmptyList_ExistId_Test() {
        List<TransactionReadDto> listFromBase = balanceService.readAllTransactionByClientId(testExistId);
        assertThat(listFromBase.size()).isGreaterThan(1);
    }

    @Test
    void readAllTransactionByClientId_ShouldReturnEmptyList_HaveNoRecordsForTestId_Test() {
        List<TransactionReadDto> listFromBase = balanceService.readAllTransactionByClientId(testNotExistId);
        assertThat(listFromBase.size()).isEqualTo(0);
    }
}