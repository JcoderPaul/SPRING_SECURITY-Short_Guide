package me.oldboy.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.dto.auth_dto.ClientAuthRequest;
import me.oldboy.filters.request_wrapper.CachedBodyHttpServletRequest;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.services.ClientService;
import me.oldboy.validation.ValidatorFilterDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPassValidatorAndAfterLogoutFilterTest {

    @Mock
    private MockHttpServletRequest mockRequest;
    @Mock
    private MockHttpServletResponse mockResponse;
    @Mock
    private FilterChain mockChain;
    @Mock
    private ClientService mockClientService;
    @Mock
    private UserDetailsService mockUserDetailsService;
    @Mock
    private ValidatorFilterDto mockValidatorFilterDto;  // Мокаем синглтон
    @Mock
    private CachedBodyHttpServletRequest testCachedRequest;
    @InjectMocks
    private UserPassValidatorAndAfterLogoutFilter userPassValidatorAndAfterLogoutFilter;

    private Client testClient;
    private ClientAuthRequest testClientAuthRequest;
    private String testJsonRequestAndCachedBody, shouldNotFilterPath, shouldFilterPath;
    private UserDetails testUserDetails;

    @BeforeAll
    static void setUpDtoValidator(){
        /* Мокаем статику ValidatorFilterDto */
        mockStatic(ValidatorFilterDto.class);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();

        /* Мокаем вызов *.getInstance() для ValidatorFilterDto */
        when(ValidatorFilterDto.getInstance()).thenReturn(mockValidatorFilterDto);

        testJsonRequestAndCachedBody = "{\"username\": \"" + EXIST_EMAIL + "\", \"password\": \"" + TEST_PASS + "\"}";
        testClientAuthRequest = new ClientAuthRequest(EXIST_EMAIL, TEST_PASS);

        testClient = Client.builder()
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .build();

        testUserDetails = new SecurityClientDetails(testClient);
        shouldFilterPath = "/api/loginClient";
        shouldNotFilterPath = "/api/regClient";
    }

    @AfterEach
    void clearSecurityContext(){
        SecurityContextHolder.clearContext();
    }

    /* Тест большой и тут надо внимательно следить за тем, что и как "мокается" и вызывается */
    @Test
    @SneakyThrows
    void doFilterInternal_SuccessLoginClient_Test() {
        /* Мокаем HttpServletRequest - запрос и задаем ожидаемые параметры */
        mockRequest.setServletPath(shouldFilterPath);
        mockRequest.setContentType("application/json");
        mockRequest.setContent(testJsonRequestAndCachedBody.getBytes());

        /* Мокаем валидатор тела запроса */
        doNothing().when(mockValidatorFilterDto).isValidData(any(ClientAuthRequest.class));
        when(mockClientService.getClientIfAuthDataCorrect(any(ClientAuthRequest.class))).thenReturn(Optional.of(testClient));

        when(mockUserDetailsService.loadUserByUsername(EXIST_EMAIL)).thenReturn(testUserDetails);

        /* Вызов тестируемого метода */
        userPassValidatorAndAfterLogoutFilter.doFilterInternal(mockRequest, mockResponse, mockChain);

        /* Убеждаемся, что SecurityContextHolder установлен и проверяем объект аутентификации */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(EXIST_EMAIL).isEqualTo(authentication.getName());
        assertThat(Collections.singletonList(new SimpleGrantedAuthority("USER"))).isEqualTo(authentication.getAuthorities());

        /* Убеждаемся, что chain.doFilter был вызван именно с CachedBodyHttpServletRequest (т.е. с нашим "кэшером запроса") */
        verify(mockChain, times(1)).doFilter(any(CachedBodyHttpServletRequest.class), eq(mockResponse));

        /* Убедитесь, что методы сервисов и валидатора были вызваны "нужное количество раз" */
        verify(mockValidatorFilterDto, times(1)).isValidData(any(ClientAuthRequest.class));
        verify(mockClientService, times(1)).getClientIfAuthDataCorrect(argThat(req ->
                req.getUsername().equals(EXIST_EMAIL) && req.getPassword().equals(TEST_PASS)));
        verify(mockUserDetailsService, times(1)).loadUserByUsername(EXIST_EMAIL);
    }

    @Test
    @SneakyThrows
    void doFilterInternal_NotLoginClientPath_Test() {
        /* Мокаем HttpServletRequest - запрос и задаем ожидаемые параметры */
        mockRequest.setServletPath("/api/myAccount");
        mockRequest.setContentType("application/json");
        mockRequest.setContent(testJsonRequestAndCachedBody.getBytes());

        /* Вызов тестируемого метода */
        userPassValidatorAndAfterLogoutFilter.doFilterInternal(mockRequest, mockResponse, mockChain);

        /* Убеждаемся, что SecurityContextHolder установлен и проверяем объект аутентификации */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNull();

        /* Убеждаемся, что chain.doFilter был вызван именно с CachedBodyHttpServletRequest (т.е. с нашим "кэшером запроса") */
        verify(mockChain, times(1)).doFilter(any(CachedBodyHttpServletRequest.class), eq(mockResponse));
    }

    @Test
    @SneakyThrows
    void doFilterInternal_EmptyRequestBody_Test() {
        /* Мокаем HttpServletRequest - запрос и задаем ожидаемые параметры */
        mockRequest.setServletPath(shouldFilterPath);

        /* Вызов тестируемого метода */
        userPassValidatorAndAfterLogoutFilter.doFilterInternal(mockRequest, mockResponse, mockChain);

        /* Убеждаемся, что SecurityContextHolder установлен и проверяем объект аутентификации */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNull();
        assertThat(mockResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);

        /* Убеждаемся, что chain.doFilter не вызывался вовсе */
        verifyNoInteractions(mockChain);
    }

    @Test
    @SneakyThrows
    void doFilterInternal_ValidLoginDataBut_Not_AuthClient_Test() {
        /* Мокаем HttpServletRequest - запрос и задаем ожидаемые параметры */
        mockRequest.setServletPath(shouldFilterPath);
        mockRequest.setContentType("application/json");
        mockRequest.setContent(testJsonRequestAndCachedBody.getBytes());

        /* Мокаем валидатор тела запроса */
        doNothing().when(mockValidatorFilterDto).isValidData(any(ClientAuthRequest.class));
        when(mockClientService.getClientIfAuthDataCorrect(any(ClientAuthRequest.class))).thenReturn(Optional.empty());

        /* Вызов тестируемого метода */
        userPassValidatorAndAfterLogoutFilter.doFilterInternal(mockRequest, mockResponse, mockChain);

        /* Убеждаемся, что SecurityContextHolder установлен и проверяем объект аутентификации */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNull();

        /* Убеждаемся, что chain.doFilter был вызван именно с CachedBodyHttpServletRequest (т.е. с нашим "кэшером запроса") */
        verify(mockChain, times(1)).doFilter(any(CachedBodyHttpServletRequest.class), eq(mockResponse));

        /* Убедитесь, что методы сервисов и валидатора были вызваны "нужное количество раз" */
        verify(mockValidatorFilterDto, times(1)).isValidData(any(ClientAuthRequest.class));
        verify(mockClientService, times(1)).getClientIfAuthDataCorrect(argThat(req ->
                req.getUsername().equals(EXIST_EMAIL) && req.getPassword().equals(TEST_PASS)));
    }

    @Test
    void shouldNotFilter_ReturnsTrue_ForNotFilterPathTwo_Test() {
        mockRequest.setServletPath(shouldNotFilterPath);
        assertThat(userPassValidatorAndAfterLogoutFilter.shouldNotFilter(mockRequest)).isTrue();
    }

    @Test
    void shouldNotFilter_ReturnsFalse_ForOtherPaths_Test() {
        mockRequest.setServletPath(shouldFilterPath);
        assertThat(userPassValidatorAndAfterLogoutFilter.shouldNotFilter(mockRequest)).isFalse();
    }

    @Test
    void shouldNotFilter_ReturnsFalse_ForRootPath_Test() {
        mockRequest.setServletPath("/");
        assertThat(userPassValidatorAndAfterLogoutFilter.shouldNotFilter(mockRequest)).isFalse();
    }

    @Test
    void shouldNotFilter_ReturnsFalse_ForEmptyPath_Test() {
        mockRequest.setServletPath("");
        assertThat(userPassValidatorAndAfterLogoutFilter.shouldNotFilter(mockRequest)).isFalse();
    }
}