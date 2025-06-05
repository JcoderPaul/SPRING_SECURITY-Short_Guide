package me.oldboy.unit.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.SneakyThrows;
import me.oldboy.filters.MyAuthoritiesLoggingAfterFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith({OutputCaptureExtension.class})
class MyAuthoritiesLoggingAfterFilterTest {

    @Mock
    private ServletRequest request;
    @Mock
    private ServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private MyAuthoritiesLoggingAfterFilter myAuthLoggingAfterFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SneakyThrows
    public void shouldReturnLogMessage_DoFilterWithAuthentication_Test(CapturedOutput output) {
        when(authentication.getName()).thenReturn(EXIST_EMAIL); // Настраиваем мок аутентификации
        doReturn(Collections.singletonList((GrantedAuthority) () -> "ROLE_USER")).when(authentication).getAuthorities();

        SecurityContextHolder.getContext().setAuthentication(authentication);   // Устанавливаем аутентификацию в SecurityContext

        myAuthLoggingAfterFilter.doFilter(request, response, chain);    // Вызываем тестируемый метод

        verify(chain, times(1)).doFilter(request, response);  // Проверяем, что chain.doFilter был вызван

        /* Здесь можно также проверить логирование, если нужно (например, с помощью Logback или Mockito для логгера) */
        assertThat(output).contains(" \n *** 4 - Log MyAuthoritiesLoggingAfterFilter method *** \n " +
                " *** User " + EXIST_EMAIL + " is successfully authenticated and " +
                "has the authorities: " + authentication.getAuthorities().toString() + " *** ");
    }

    /* SecurityContext пустой (аутентификация = null) */
    @Test
    @SneakyThrows
    public void noLogMessage_DoFilterWithoutAuthentication_Test() {
        myAuthLoggingAfterFilter.doFilter(request, response, chain); // Вызываем метод

        verify(chain, times(1)).doFilter(request, response);  // Проверяем, что chain.doFilter был вызван

        verifyNoInteractions(authentication);   // Проверяем, что не было попыток получить имя или authorities
    }
}