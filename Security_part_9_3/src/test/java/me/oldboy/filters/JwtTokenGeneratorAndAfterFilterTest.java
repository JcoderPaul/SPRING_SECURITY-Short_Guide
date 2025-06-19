package me.oldboy.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.constants.SecurityConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenGeneratorAndAfterFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockChain;
    @Mock
    private Authentication mockAuthentication, mockZeroAuthentication;
    @InjectMocks
    private JwtTokenGeneratorAndAfterFilter jwtTokenGeneratorAndAfterFilter;

    private String testPath, testNotFilterPath;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        UserDetails principal = User.builder()
                .username(EXIST_EMAIL)
                .password(TEST_PASS)
                .roles(TEST_STR_ROLE_ADMIN)
                .authorities("READ")
                .build();

        mockAuthentication = new TestingAuthenticationToken(principal, TEST_PASS, principal.getAuthorities());
        mockZeroAuthentication = null;

        testPath = "/api/myBalance";
        testNotFilterPath = "/api/loginClient";
    }

    @AfterEach
    void eraseSecurityContext(){
        SecurityContextHolder.clearContext();
    }

    @Test
    @SneakyThrows
    void withJwtTokenResponse_WithAuth_DoFilterInternal_Test() {
        /* Настраиваем "заглушки" */
        when(mockRequest.getServletPath()).thenReturn(testPath);

        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);   // Устанавливаем аутентификацию в SecurityContext

        jwtTokenGeneratorAndAfterFilter.doFilter(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        assertThat(mockResponse.getHeader(SecurityConstants.JWT_HEADER)).isNullOrEmpty();   // Проверяем, что нужный Header установлен

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockRequest, times(1)).getServletPath();
    }

    @Test
    @SneakyThrows
    void withoutJwtResponse_NoAuth_DoFilterInternal_Test() {
        /* Настраиваем "заглушки" */
        when(mockRequest.getServletPath()).thenReturn(testPath);

        SecurityContextHolder.getContext().setAuthentication(mockZeroAuthentication);   // Устанавливаем аутентификацию в SecurityContext

        jwtTokenGeneratorAndAfterFilter.doFilter(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        assertThat(mockResponse.getHeader(SecurityConstants.JWT_HEADER)).isNull();

        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockRequest, times(1)).getServletPath();
    }

    /*
        Для тестирования protected методов необходимо поместить наши тесты в тот же
        пакет, что и тестируемые классы - https://junit.org/junit4/faq.html#organize_1
    */
    @Test
    void shouldNotFilter_ReturnsFalse_ForLoginClientPath_Test() {
        when(mockRequest.getServletPath()).thenReturn(testNotFilterPath);
        assertThat(jwtTokenGeneratorAndAfterFilter.shouldNotFilter(mockRequest)).isFalse();
    }

    @Test
    void shouldNotFilter_ReturnsTrue_ForOtherPaths_Test() {
        when(mockRequest.getServletPath()).thenReturn(testPath);
        assertThat(jwtTokenGeneratorAndAfterFilter.shouldNotFilter(mockRequest)).isTrue();
    }

    @Test
    void shouldNotFilter_ReturnsTrueForRootPath_Test() {
        when(mockRequest.getServletPath()).thenReturn("/");
        assertThat(jwtTokenGeneratorAndAfterFilter.shouldNotFilter(mockRequest)).isTrue();
    }

    @Test
    void shouldNotFilter_ReturnsTrueForEmptyPath_Test() {
        when(mockRequest.getServletPath()).thenReturn("");
        assertThat(jwtTokenGeneratorAndAfterFilter.shouldNotFilter(mockRequest)).isTrue();
    }
}