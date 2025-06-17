package me.oldboy.unit.controllers.util;

import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.exception.EmptyCurrentClientException;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.models.money.Account;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class UserDetailsDetectorBadTest {

    @Mock
    private ClientService mockClientService;
    @Mock
    private Authentication mockAuthentication;
    private UserDetailsDetector userDetailsDetector;

    private Client testClient;
    private Account testAccount;
    private Long testId;
    private Boolean isTestUserNotNull;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testId  = 1L;

        testAccount = Account.builder()
                .client(testClient)
                .accountNumber(1212342134L)
                .accountId(testId)
                .build();

        testClient = Client.builder()
                .id(testId)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .account(testAccount)
                .build();

        userDetailsDetector = new UserDetailsDetector();

        when(mockAuthentication.getName()).thenReturn(null);
        when(mockClientService.findByEmail(EXIST_EMAIL)).thenReturn(Optional.empty());
        isTestUserNotNull = userDetailsDetector.isUserDetailsNotNull(mockClientService, mockAuthentication);
    }

    @Test
    void shouldReturnFalse_IsUserDetailsNotNull_Test() {
        assertThat(isTestUserNotNull).isFalse();
    }

    @Test
    void shouldReturnEqNull_GetClientEmail_Test() {
        String mayBeEmail = userDetailsDetector.getClientEmail();

        assertThat(mayBeEmail).isEqualTo(null);
    }

    @Test
    void shouldReturnEqNull_GetClientId_Test() {
        Long mayBeId = userDetailsDetector.getClientId();

        assertThat(mayBeId).isEqualTo(null);
    }

    @Test
    void shouldReturnEqNull_GetAccountNumber_Test() {
        Long mayBeAccount = userDetailsDetector.getAccountNumber();

        assertThat(mayBeAccount).isEqualTo(null);
    }

    @Test
    void shouldReturnException_GetCurrentClient_Test() {
        assertThatThrownBy(() -> userDetailsDetector.getCurrentClient())
                .isInstanceOf(EmptyCurrentClientException.class)
                .hasMessageContaining("Have no Client from UserDetailsDetector!");
    }
}