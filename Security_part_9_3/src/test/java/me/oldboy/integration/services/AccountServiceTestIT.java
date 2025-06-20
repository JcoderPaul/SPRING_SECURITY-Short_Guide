package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.exception.AccountServiceException;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@IT
class AccountServiceTestIT extends TestContainerInit {

    @Autowired
    private AccountService accountService;

    private Long testIdExist, testIdNotExist;

    @BeforeEach
    void setUp(){
        testIdExist = 1L;
        testIdNotExist = 100L;
    }

    @Test
    void readAccountByClientId_ShouldReturnAccountByTestId_Test() {
        AccountReadDto accountDtoFromBase = accountService.readAccountByClientId(testIdExist);

        assertAll(
                () -> assertThat(accountDtoFromBase).isNotNull(),
                () -> assertThat(accountDtoFromBase.getAccountType()).isEqualTo("Savings"),
                () -> assertThat(accountDtoFromBase.getBranchAddress()).isEqualTo("123 Main Street, New York")
        );
    }

    @Test
    void readAccountByClientId_ShouldReturnException_Test() {
        assertThatThrownBy(() -> accountService.readAccountByClientId(testIdNotExist))
                .isInstanceOf(AccountServiceException.class)
                .hasMessageContaining("Client with ID " + testIdNotExist + " have no account!");
    }
}