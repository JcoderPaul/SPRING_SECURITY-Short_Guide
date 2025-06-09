package me.oldboy.unit.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.constants.SecurityConstants;
import me.oldboy.filters.JwtTokenValidatorAndBeforeFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class JwtTokenValidatorAndBeforeFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockChain;
    @InjectMocks
    private JwtTokenValidatorAndBeforeFilter jwtTokenValidatorAndBeforeFilter;

    private String testToken, badToken;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImF1dGhvcml0aWVzIjoiTU" +
                "9SRSBCQUQgQUNUSU9OLEFETUlOIn0.LImgJxRtwjdDFPdy5od6W5AgeDV7o7t-gAq-vFwqGwY";
        badToken = "bad_token_really_bad";
    }

    @Test
    @SneakyThrows
    void correctIncomingJwtToken_DoFilterInternal_Test(CapturedOutput output) {
        /* Настраиваем "заглушки" */
        when(mockRequest.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(testToken);

        jwtTokenValidatorAndBeforeFilter.doFilter(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        /* Проверяем логи */
        assertThat(output).contains("-- 2 - Start JwtTokenValidator or BeforeFilter");

        assertThat(output).contains("-- 2 - Finish JwtTokenValidator or BeforeFilter");

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockRequest, times(1)).getHeader(SecurityConstants.JWT_HEADER);
    }

    @Test
    @SneakyThrows
    void badIncomingJwtToken_ReturnException_DoFilterInternal_Test(CapturedOutput output) {
        /* Настраиваем "заглушки" */
        when(mockRequest.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(badToken);

        assertThatThrownBy(() -> jwtTokenValidatorAndBeforeFilter.doFilter(mockRequest, mockResponse, mockChain))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid Token received!");

        /* Проверяем логи */
        assertThat(output).contains("-- 2 - Start JwtTokenValidator or BeforeFilter");

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockRequest, times(1)).getHeader(SecurityConstants.JWT_HEADER);
    }
}