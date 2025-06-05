package me.oldboy.unit.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.filters.MySecondRequestValidationBeforeFilter;
import me.oldboy.filters.utils.RememberMeUserNameExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith({OutputCaptureExtension.class})
class MySecondRequestValidationBeforeFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;
    @Mock
    private RememberMeUserNameExtractor rememberMeUserNameExtractor;
    @InjectMocks
    private MySecondRequestValidationBeforeFilter mySecondRequestValidationBeforeFilter;

    private Cookie[] cookies, withRememberMeCookie;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.clearContext(); // Очищаем контекст перед каждым тестом

        cookies = new Cookie[]{new Cookie("other-cookie", "value")};
        withRememberMeCookie = new Cookie[]{new Cookie("remember-me", "value")};
    }

    @Test
    @SneakyThrows
    public void haveNoAnyCookie_And_LogNoRememberMe_DoFilterTest(CapturedOutput output) {
        when(mockRequest.getCookies()).thenReturn(null);

        mySecondRequestValidationBeforeFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);

        assertThat(output).contains(" \n *** 2 - Log MySecondRequestValidationBeforeFilter method *** \n" +
                " *** User try to authentication! Have no Remember-Me cookies! *** ");
    }

    @Test
    @SneakyThrows
    public void haveNoRememberMeCookie_And_LogIt_DoFilterTest(CapturedOutput output) {
        when(mockRequest.getCookies()).thenReturn(cookies);

        mySecondRequestValidationBeforeFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);

        assertThat(output).contains(" \n *** 2 - Log MySecondRequestValidationBeforeFilter method *** \n" +
                " *** User try to authentication! Have no Remember-Me cookies! *** ");
    }

    @Test
    @SneakyThrows
    public void hasRememberMeCookie_And_LogIt_DoFilterTest(CapturedOutput output) {
        when(mockRequest.getCookies()).thenReturn(withRememberMeCookie);
        doReturn(Optional.of(EXIST_EMAIL)).when(rememberMeUserNameExtractor).getUserNameFromToken(mockRequest);

        mySecondRequestValidationBeforeFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);

        assertThat(output).contains(" \n *** 2 - Log MySecondRequestValidationBeforeFilter method *** \n" +
                " *** User " + EXIST_EMAIL + " is already authenticated by Remember-Me Token! *** ");
    }
}