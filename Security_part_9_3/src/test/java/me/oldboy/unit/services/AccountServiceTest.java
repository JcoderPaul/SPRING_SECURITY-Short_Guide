package me.oldboy.unit.services;

import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.exception.AccountServiceException;
import me.oldboy.mapper.AccountMapper;
import me.oldboy.models.money.Account;
import me.oldboy.repository.AccountRepository;
import me.oldboy.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository mockAccountRepository;
    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private AccountReadDto testAccountReadDto;
    private Long testId, testAccountNumber;
    private String testAccountType;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(true);

        testId = 1L;
        testAccountNumber = 123455667L;
        testAccountType = "Saving";

        testAccount = Account.builder()
                .accountId(testId)
                .accountNumber(testAccountNumber)
                .accountType(testAccountType)
                .build();
    }

    @Test
    void shouldReturnEq_ReadAccountByClientId_Test() {
        AccountReadDto originalAccountDto = AccountMapper.INSTANCE.mapToAccountReadDto(testAccount);
        when(mockAccountRepository.findByClientId(testId)).thenReturn(Optional.of(testAccount));

        assertThat(accountService.readAccountByClientId(testId)).isEqualTo(originalAccountDto);

        verify(mockAccountRepository, times(1)).findByClientId(anyLong());
    }

    @Test
    void shouldReturnException_ReadAccountByClientId_Test() {
        when(mockAccountRepository.findByClientId(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.readAccountByClientId(testId))
                .isInstanceOf(AccountServiceException.class)
                .hasMessageContaining("Client with ID " + testId + " have no account!");

        verify(mockAccountRepository, times(1)).findByClientId(anyLong());
    }
}