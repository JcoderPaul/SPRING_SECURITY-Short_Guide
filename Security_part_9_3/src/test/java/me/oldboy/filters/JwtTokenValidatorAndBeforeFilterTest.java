package me.oldboy.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static me.oldboy.test_constant.TestConstantFields.*;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorAndBeforeFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockChain;
    @Mock
    private Authentication mockAuthentication;
    @InjectMocks
    private JwtTokenValidatorAndBeforeFilter jwtTokenValidatorAndBeforeFilter;

    private String testFilterPath, testNotFilterPathOne, testNotFilterPathTwo, jwtToken, headerPrefix;
    private SecretKey key;
    private Claims currentClaims;

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

        testFilterPath = "/api/myBalance";
        testNotFilterPathOne = "/api/regClient";
        testNotFilterPathTwo = "/api/loginClient";

        headerPrefix = "Bearer ";
    }

    @AfterEach
    void eraseSecurityContext(){
        SecurityContextHolder.clearContext();
    }

    /* Должен аутентифицировать пользователя с валидным JWT */
    @Test
    @SneakyThrows
    void shouldReturnAuthUserName_DoFilterInternal_Test() {
        /* Настраиваем "заглушки" */
        jwtToken = generateTestJwtToken(SecurityConstants.JWT_KEY);
        when(mockRequest.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(headerPrefix + jwtToken);

        jwtTokenValidatorAndBeforeFilter.doFilterInternal(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(EXIST_EMAIL);
        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    /* Должен бросить исключение с невалидным JWT */
    @Test
    @SneakyThrows
    void shouldReturnException_NotValidToken_DoFilterInternal_Test() {
        /* Настраиваем "заглушки" */
        when(mockRequest.getServletPath()).thenReturn(testFilterPath);
        when(mockRequest.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(headerPrefix + "not valid token");

        assertThatThrownBy(() -> jwtTokenValidatorAndBeforeFilter.doFilter(mockRequest, mockResponse, mockChain))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid Token received!");    // Вызываем тестируемый метод

        /* Проверяем сколько раз запускался тот или иной метод */
        verifyNoInteractions(mockChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /* НЕ должен аутентифицировать пользователя повторно если он уже аутентифицирован */
    @Test
    @SneakyThrows
    void shouldReturnAlreadyAuthUser_DoFilterInternal_Test() {
        /* Настраиваем "заглушки" */
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);

        jwtTokenValidatorAndBeforeFilter.doFilterInternal(mockRequest, mockResponse, mockChain);    // Вызываем тестируемый метод

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(EXIST_EMAIL);
        /* Проверяем сколько раз запускался тот или иной метод */
        verify(mockChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    /* Должен бросить исключение с невалидным JWT */
    @Test
    @SneakyThrows
    void shouldReturnException_IllegalKeySignedToken_DoFilterInternal_Test() {
        when(mockRequest.getServletPath()).thenReturn(testFilterPath);
        jwtToken = generateTestJwtToken("secret_key_of_the_bad_guys_who_want_to_break_into_our_network");
        /* Настраиваем "заглушки" */
        when(mockRequest.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(headerPrefix + jwtToken);

        assertThatThrownBy(() -> jwtTokenValidatorAndBeforeFilter.doFilter(mockRequest, mockResponse, mockChain))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid Token received!");    // Вызываем тестируемый метод

        /* Проверяем сколько раз запускался тот или иной метод */
        verifyNoInteractions(mockChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /*
       Для тестирования protected методов необходимо поместить наши тесты в тот же
       пакет, что и тестируемые классы - https://junit.org/junit4/faq.html#organize_1
   */
    @Test
    void shouldNotFilter_ReturnsTrue_ForNotFilterPathOne_Test() {
        when(mockRequest.getServletPath()).thenReturn(testNotFilterPathOne);
        assertThat(jwtTokenValidatorAndBeforeFilter.shouldNotFilter(mockRequest)).isTrue();
    }

    @Test
    void shouldNotFilter_ReturnsTrue_ForNotFilterPathTwo_Test() {
        when(mockRequest.getServletPath()).thenReturn(testNotFilterPathTwo);
        assertThat(jwtTokenValidatorAndBeforeFilter.shouldNotFilter(mockRequest)).isTrue();
    }

    @Test
    void shouldNotFilter_ReturnsFalse_ForOtherPaths_Test() {
        when(mockRequest.getServletPath()).thenReturn(testFilterPath);
        assertThat(jwtTokenValidatorAndBeforeFilter.shouldNotFilter(mockRequest)).isFalse();
    }

    @Test
    void shouldNotFilter_ReturnsFalse_ForRootPath_Test() {
        when(mockRequest.getServletPath()).thenReturn("/");
        assertThat(jwtTokenValidatorAndBeforeFilter.shouldNotFilter(mockRequest)).isFalse();
    }

    @Test
    void shouldNotFilter_ReturnsFalse_ForEmptyPath_Test() {
        when(mockRequest.getServletPath()).thenReturn("");
        assertThat(jwtTokenValidatorAndBeforeFilter.shouldNotFilter(mockRequest)).isFalse();
    }

    /* Генерируем token - как в JwtTokenGeneratorAndAfterFilter фильтре */
    private String generateTestJwtToken(String secretKey){
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        currentClaims = Jwts.claims()
                .add("username", EXIST_EMAIL)
                .add("authorities", TEST_STR_ROLE_ADMIN)
                .build();

        String testJwtToken = Jwts.builder()
                .claims(currentClaims)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + 300_000_000))
                .signWith(key)
                .compact();

        return testJwtToken;
    }
}