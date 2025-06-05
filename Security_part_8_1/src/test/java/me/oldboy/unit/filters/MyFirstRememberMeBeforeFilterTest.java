package me.oldboy.unit.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.filters.MyFirstRememberMeBeforeFilter;
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
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith({OutputCaptureExtension.class})
class MyFirstRememberMeBeforeFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private MyFirstRememberMeBeforeFilter myFirstRememberMeBeforeFilter;

    private Cookie[] cookies;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext(); // Очищаем контекст перед каждым тестом

        cookies = new Cookie[]{new Cookie("other-cookie", "value")};
    }

    @Test
    @SneakyThrows
    public void haveNoAnyCookie_And_LogNoRememberMe_DoFilterTest(CapturedOutput output) {
        when(mockRequest.getCookies()).thenReturn(null);

        myFirstRememberMeBeforeFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);

        assertThat(output).contains(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
                " *** User try to authentication! Have no Remember-Me cookies! *** ");
    }

    @Test
    @SneakyThrows
    public void haveNoRememberMeCookie_And_LogIt_DoFilterTest(CapturedOutput output) {
        when(mockRequest.getCookies()).thenReturn(cookies);

        myFirstRememberMeBeforeFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);

        assertThat(output).contains(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
                " *** User try to authentication! Have no Remember-Me cookies! *** ");
    }

    @Test
    public void hasRememberMeCookie_ButNoAuth_And_LogIt_DoFilterTest(CapturedOutput output) throws IOException, ServletException {
        Cookie[] cookies = {new Cookie("remember-me", "token-value")};
        when(mockRequest.getCookies()).thenReturn(cookies);

        myFirstRememberMeBeforeFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);

        assertThat(output).contains(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
                " *** User is already authenticated by Remember-Me Token! *** ");
    }

    @Test
    public void hasRememberMeCookie_hasAuth_LogIt_DoFilterTest(CapturedOutput output) throws IOException, ServletException {
        Cookie[] cookies = {new Cookie("remember-me", "token-value")};
        when(mockRequest.getCookies()).thenReturn(cookies);
        when(authentication.getName()).thenReturn(EXIST_EMAIL);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        myFirstRememberMeBeforeFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);

        assertThat(output).contains(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
                " *** User is already authenticated by Remember-Me Token! *** ");

        assertThat(output).contains(" \n *** 1 - Log MyFirstRememberMeBeforeFilter method *** \n" +
                " *** User " + authentication.getName() +
                " is already authenticated by Remember-Me Token! *** ");
    }
}