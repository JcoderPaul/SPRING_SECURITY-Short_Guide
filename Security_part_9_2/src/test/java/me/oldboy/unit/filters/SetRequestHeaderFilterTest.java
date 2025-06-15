package me.oldboy.unit.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.filters.SetRequestHeaderFilter;
import me.oldboy.filters.utils.JwtSaver;
import me.oldboy.http_servlet_wrapper.CustomHttpServletRequestWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static me.oldboy.constants.SecurityConstants.EMAIL_COOKIE;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class SetRequestHeaderFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockChain;
    @Mock
    private JwtSaver mockJwtSaver;
    @InjectMocks
    private SetRequestHeaderFilter setRequestHeaderFilter;

    private String testToken;
    private Cookie[] testCookie;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImF1dGhvcml0aWVzIjoiTU" +
                "9SRSBCQUQgQUNUSU9OLEFETUlOIn0.LImgJxRtwjdDFPdy5od6W5AgeDV7o7t-gAq-vFwqGwY";

        testCookie = new Cookie[] {new Cookie(EMAIL_COOKIE, EXIST_EMAIL),
                new Cookie("cookieOne", "cookieValue1"),
                new Cookie("cookieTwo", "cookieValue2")};
    }

    @Test
    @SneakyThrows
    void getCorrectTokenFromSaver_DoFilter_Test(CapturedOutput output) {
        /* Настраиваем "заглушки" */
        when(mockRequest.getCookies()).thenReturn(testCookie);
        when(mockJwtSaver.getSavedJwt(EXIST_EMAIL)).thenReturn(testToken);

        setRequestHeaderFilter.doFilter(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        /* Проверяем логи */
        assertThat(output).contains("-- 1 - Start set request header filter");

        assertThat(output).contains("-- 1 - Set request header filter: " + null);

        assertThat(output).contains("-- 1 - Finish set request header filter: " + testToken);

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(any(CustomHttpServletRequestWrapper.class), any(HttpServletResponse.class));
        verify(mockRequest, times(1)).getCookies();
        verify(mockJwtSaver, times(1)).getSavedJwt(EXIST_EMAIL);
    }

    @Test
    @SneakyThrows
    void haveNoTokenInSaver_DoFilter_Test(CapturedOutput output) {
        /* Настраиваем "заглушки" */
        when(mockRequest.getCookies()).thenReturn(testCookie);
        when(mockJwtSaver.getSavedJwt(EXIST_EMAIL)).thenReturn(null);

        setRequestHeaderFilter.doFilter(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        /* Проверяем логи */
        assertThat(output).contains("-- 1 - Start set request header filter");

        assertThat(output).contains("-- 1 - Set request header filter: " + null);

        assertThat(output).contains("-- 1 - Finish set request header filter" + " - have no JWT!");

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockRequest, times(1)).getCookies();
        verify(mockJwtSaver, times(1)).getSavedJwt(EXIST_EMAIL);
    }
}