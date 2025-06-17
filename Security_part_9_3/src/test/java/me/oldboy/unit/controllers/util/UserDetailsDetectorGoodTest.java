package me.oldboy.unit.controllers.util;

import me.oldboy.controllers.util.UserDetailsDetector;
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
import static org.mockito.Mockito.when;

class UserDetailsDetectorGoodTest {

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

        when(mockAuthentication.getName()).thenReturn(EXIST_EMAIL);
        when(mockClientService.findByEmail(EXIST_EMAIL)).thenReturn(Optional.of(testClient));
        isTestUserNotNull = userDetailsDetector.isUserDetailsNotNull(mockClientService, mockAuthentication);
    }

    @Test
    void shouldReturnTrue_IsUserDetailsNotNull_Test() {
        assertThat(isTestUserNotNull).isTrue();
    }

    @Test
    void shouldReturnEq_GetClientEmail_Test() {
        String mayBeEmail = userDetailsDetector.getClientEmail();

        assertThat(mayBeEmail).isEqualTo(testClient.getEmail());
    }

    @Test
    void shouldReturnEq_GetClientId_Test() {
        Long mayBeId = userDetailsDetector.getClientId();

        assertThat(mayBeId).isEqualTo(testId);
    }

    @Test
    void shouldReturnEq_GetAccountNumber_Test() {
        Long mayBeAccount = userDetailsDetector.getAccountNumber();

        assertThat(mayBeAccount).isEqualTo(testAccount.getAccountNumber());
    }

    @Test
    void shouldReturnEq_GetCurrentClient_Test() {
        Client mayBeClient = userDetailsDetector.getCurrentClient();

        assertThat(mayBeClient).isEqualTo(testClient);
    }
}