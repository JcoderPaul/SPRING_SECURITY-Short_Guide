package me.oldboy.unit.services;

import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.models.money.Transaction;
import me.oldboy.repository.BalanceRepository;
import me.oldboy.services.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private BalanceRepository mockBalanceRepository;
    @InjectMocks
    private BalanceService balanceService;

    private TransactionReadDto testTransactionReadDto;
    private Transaction testTransaction;
    private List<TransactionReadDto> testTransactionDtoList;
    private List<Transaction> testTransactionList;
    private Long testId;
    private String trSummary, trType;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testId = 1L;
        trSummary = "Uber";
        trType = "Withdrawal";

        testTransaction = Transaction.builder()
                .transactionType(trType)
                .transactionSummary(trSummary)
                .build();
        testTransactionReadDto = TransactionReadDto.builder()
                .transactionType(trType)
                .transactionSummary(trSummary)
                .build();
        testTransactionList = List.of(testTransaction);
        testTransactionDtoList = List.of(testTransactionReadDto);
    }

    @Test
    void shouldReturnEqList_ReadAllTransactionByClientId_Test() {
        when(mockBalanceRepository.findByClientId(testId)).thenReturn(Optional.of(testTransactionList));

        assertThat(balanceService.readAllTransactionByClientId(testId)).isEqualTo(testTransactionDtoList);

        verify(mockBalanceRepository, times(1)).findByClientId(anyLong());
    }

    @Test
    void shouldReturnEmptyList_ReadAllTransactionByClientId_Test() {
        when(mockBalanceRepository.findByClientId(testId)).thenReturn(Optional.empty());

        assertThat(balanceService.readAllTransactionByClientId(testId)).isEqualTo(List.of());
        assertThat(balanceService.readAllTransactionByClientId(testId).size()).isEqualTo(0);

        verify(mockBalanceRepository, times(2)).findByClientId(anyLong());
    }

}