package me.oldboy.unit.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.filters.JwtTokenGeneratorAndAfterFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class JwtTokenGeneratorAndAfterFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockChain;
    @Mock
    private AuthenticationEventListener mockAuthenticationEventListener;
    @Mock
    private Authentication mockAuthentication;
    @InjectMocks
    private JwtTokenGeneratorAndAfterFilter jwtTokenGeneratorAndAfterFilter;

    private String testPath, testPathNotHtml;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        UserDetails principal = User.builder()
                .username(EXIST_EMAIL)
                .password(TEST_PASS)
                .roles(TEST_STR_ROLE_ADMIN)
                .authorities("READ")
                .build();

        mockAuthentication = new UsernamePasswordAuthenticationToken(principal, TEST_PASS, principal.getAuthorities());

        testPath = "/webui/myLoans";
        testPathNotHtml = "/css/main_menu.css";
    }

    @Test
    @SneakyThrows
    void withZeroingAuthenticationEventListener_DoFilterInternal_Test(CapturedOutput output) {
        /* Настраиваем "заглушки" */
        when(mockAuthenticationEventListener.getAuthenticationAfterFormLogin()).thenReturn(mockAuthentication);
        when(mockRequest.getServletPath()).thenReturn(testPath);

        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);   // Устанавливаем аутентификацию в SecurityContext

        jwtTokenGeneratorAndAfterFilter.doFilter(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        /* Проверяем логи */
        assertThat(output).contains("-- 3 - Start JwtTokenGenerator or AfterFilter");

        /* В наличии - "contains" */
        assertThat(output).contains("URL from JwtTokenGenerator or AfterFilter: " + testPath);
        assertThat(output).contains("After zeroing authFromForm: " + null);

        assertThat(output).contains("-- 3 - Finish JwtTokenGenerator or AfterFilter");

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockRequest, times(2)).getServletPath();
    }

    @Test
    @SneakyThrows
    void with_No_ZeroingAuthenticationEventListener_DoFilterInternal_Test(CapturedOutput output) {
        /* Настраиваем "заглушки" */
        when(mockAuthenticationEventListener.getAuthenticationAfterFormLogin()).thenReturn(mockAuthentication);
        when(mockRequest.getServletPath()).thenReturn(testPathNotHtml);

        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);   // Устанавливаем аутентификацию в SecurityContext

        jwtTokenGeneratorAndAfterFilter.doFilter(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        /* Здесь можно также проверить логирование */
        assertThat(output).contains("-- 3 - Start JwtTokenGenerator or AfterFilter");

        /* Отсутствуют - "doesNotContain" */
        assertThat(output).doesNotContain("URL from JwtTokenGenerator or AfterFilter: " + testPathNotHtml);
        assertThat(output).doesNotContain("After zeroing authFromForm: " + null);

        assertThat(output).contains("-- 3 - Finish JwtTokenGenerator or AfterFilter");

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockRequest, times(1)).getServletPath();
    }
}