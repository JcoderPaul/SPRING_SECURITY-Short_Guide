package me.oldboy.unit.services;

import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.models.money.Transaction;
import me.oldboy.repository.BalanceRepository;
import me.oldboy.services.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BalanceServiceTest {

    @Mock
    private BalanceRepository mockBalanceRepository;
    @InjectMocks
    private BalanceService balanceService;

    private List<Transaction> testList;
    private Transaction testTransaction;
    private Long clientId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        clientId = 1L;

        testTransaction = Transaction.builder()
                .closingBalance(1000)
                .createDt(LocalDate.of(2023,10,04))
                .transactionAmt(10)
                .transactionDt(LocalDate.of(2024,6,12))
                .transactionType("Перевод")
                .build();

        testList = List.of(testTransaction, new Transaction(), new Transaction());
    }

    @Test
    void shouldReturnRightListSize_ReadAllTransactionByClientId_Test() {
        when(mockBalanceRepository.findByClientId(clientId)).thenReturn(Optional.of(testList));

        var originalSize = testList.size();
        var expectedSize = balanceService.readAllTransactionByClientId(clientId).size();

        assertThat(originalSize).isEqualTo(expectedSize);
    }

    @Test
    void shouldReturnEmptyList_ReadAllTransactionByClientId_Test() {
        when(mockBalanceRepository.findByClientId(clientId)).thenReturn(Optional.empty());

        var expectedSize = balanceService.readAllTransactionByClientId(clientId).size();

        assertThat(0).isEqualTo(expectedSize);
    }

    @Test
    void shouldReturnTransactionReadDtoListElement_ReadAllTransactionByClientId_Test() {
        when(mockBalanceRepository.findByClientId(clientId)).thenReturn(Optional.of(testList));

        var isTrueOrFalse = balanceService.readAllTransactionByClientId(clientId).get(0) instanceof TransactionReadDto;

        assertThat(isTrueOrFalse).isTrue();
    }
}