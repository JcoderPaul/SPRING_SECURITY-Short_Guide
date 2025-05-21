package me.oldboy.unit.auth_provider;

import me.oldboy.config.auth_provider.CustomAuthProvider;
import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.exception.EmailNotFoundException;
import me.oldboy.models.Client;
import me.oldboy.models.Role;
import me.oldboy.test_content.TestConstantFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthProviderTest {

    @Mock
    private PasswordEncoder mockPasswordEncoder;
    @Mock
    private ClientDetailsService mockClientDetailsService;
    @InjectMocks
    private CustomAuthProvider customAuthProvider;

    private Authentication testAuthentication;
    private SecurityClientDetails testPrincipal;
    private Client testClient;
    private String testEmail = TestConstantFields.TEST_EMAIL;
    private String testPassword = TestConstantFields.TEST_PASS;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                           .id(1L)
                           .email(testEmail)
                           .pass(testPassword)
                           .role(Role.USER)
                           .build();

        testPrincipal = SecurityClientDetails.builder()
                                             .client(testClient)
                                             .build();

        /*
            Задаем объект Authentication, который ниже передаем в тестируемый метод, т.е. именно он проходит сквозь
            код этого метода, именно с его содержимым происходит вся работа в тестируемом методе. Во всех сценариях
            тестирования он может быть неизменным и только результат работы "моков" будет приводить к ожидаемому в
            тесте результату, т.к. у нас нет реальной БД и мы симулируем результат ответа от метода репозитория.
        */
        testAuthentication = new UsernamePasswordAuthenticationToken(testPrincipal, testPassword);

    }

    @Test
    void successAuthenticateTest() {
        /* Формируем mock заглушки */
        when(mockClientDetailsService.loadUserByUsername(testEmail)).thenReturn(testPrincipal);
        when(mockPasswordEncoder.matches(testPassword, testClient.getPass())).thenReturn(true);

        Authentication result = customAuthProvider.authenticate(testAuthentication);

        assertThat(result).isNotNull(); // Результат вызова метода не null
        assertThat(result.isAuthenticated()).isTrue(); // Токен был аутентифицирован
        assertThat(testPrincipal.getUsername()).isEqualTo(result.getName()); // Проверяем имена входящие и возвращаемые методом
        assertThat(1).isEqualTo(result.getAuthorities().size()); // У нас всего одно полномочие в коллекции
        assertThat(result.getAuthorities().contains(new SimpleGrantedAuthority("USER"))).isTrue(); // Проверяем какие полномочия в коллекции

        verify(mockClientDetailsService, times(1)).loadUserByUsername(testEmail);
        verify(mockPasswordEncoder, times(1)).matches(testPassword, testClient.getPass());
    }

    @Test
    void exceptionClientNameNotFoundAuthenticateTest() {
        when(mockClientDetailsService.loadUserByUsername(testEmail))
                .thenThrow(new EmailNotFoundException("User with email/name " + testPrincipal.getUsername() + " not found!"));

        assertThatThrownBy(() -> customAuthProvider.authenticate(testAuthentication))
                .isInstanceOf(EmailNotFoundException.class)
                .hasMessageContaining("User with email/name " + testPrincipal.getUsername() + " not found!");

        verify(mockClientDetailsService, times(1)).loadUserByUsername(testEmail);
    }

    @Test
    void exceptionWrongClientPasswordAuthenticateTest() {
        when(mockClientDetailsService.loadUserByUsername(testEmail)).thenReturn(testPrincipal);
        when(mockPasswordEncoder.matches(testPassword, testClient.getPass())).thenReturn(false);

        assertThatThrownBy(() -> customAuthProvider.authenticate(testAuthentication))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad password!");

        verify(mockClientDetailsService, times(1)).loadUserByUsername(testEmail);
        verify(mockPasswordEncoder, times(1)).matches(testPassword, testPassword);
    }

    /*
    Метод *.support() возвращает:
    - true - если наш провайдер аутентификации поддерживает UsernamePasswordAuthenticationToken;
    - false - если токен любого другого вида;

    проверяем:
    */
    @Test
    void isUsernamePasswordAuthenticationToken_ShouldReturnTrue_SupportsTest() {
        assertThat(customAuthProvider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
    }

    @Test
    void isOtherAuthenticationToken_ShouldReturnFalse_SupportsTest() {
        assertThat(customAuthProvider.supports(AnonymousAuthenticationToken.class)).isFalse();
    }
}