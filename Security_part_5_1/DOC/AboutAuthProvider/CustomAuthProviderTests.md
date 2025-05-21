Тестирование пользовательского AuthenticationProvider в Spring Security является важным шагом для обеспечения его 
правильной работы и безопасности. Вот несколько подходов и инструментов, которые мы можем использовать:
________________________________________________________________________________________________________________________
**1. Юнит-тестирование (Unit Testing)**

Это самый базовый уровень тестирования. Мы тестируем AuthenticationProvider в изоляции, без запуска полного контекста Spring.
Ключевые идеи:
- Изолируем логику: Фокусируемся на логике проверки учетных данных и создании объекта Authentication.
- "Мокаем" зависимости: Если наш AuthenticationProvider зависит от других компонентов (например, UserDetailsService, 
репозитория, сервиса LDAP), используем "заглушки" (Mockito) для имитации их поведения. 

**Пример (с использованием JUnit 5 и Mockito).**
Предположим, у нас есть реализация CustomAuthenticationProvider, который зависит от UserService для получения деталей пользователя:

        import org.springframework.security.authentication.AuthenticationProvider;
        import org.springframework.security.authentication.BadCredentialsException;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.security.core.Authentication;
        import org.springframework.security.core.AuthenticationException;
        import org.springframework.security.core.GrantedAuthority;
        import org.springframework.security.core.authority.SimpleGrantedAuthority;
        import org.springframework.security.core.userdetails.User;
        import org.springframework.security.core.userdetails.UserDetails;
        import org.springframework.security.core.userdetails.UserDetailsService;
        import org.springframework.security.core.userdetails.UsernameNotFoundException;
        import org.springframework.security.crypto.password.PasswordEncoder;
        import org.springframework.stereotype.Component;
        
        import java.util.Collections;
        import java.util.List;
        
        @Component
        public class CustomAuthenticationProvider implements AuthenticationProvider {
        
            private final UserDetailsService userDetailsService;
            private final PasswordEncoder passwordEncoder;
        
            public CustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
                this.userDetailsService = userDetailsService;
                this.passwordEncoder = passwordEncoder;
            }
        
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();
        
                UserDetails userDetails;
                try {
                    userDetails = userDetailsService.loadUserByUsername(username);
                } catch (UsernameNotFoundException e) {
                    throw new BadCredentialsException("Пользователь не найден");
                }
        
                if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                    throw new BadCredentialsException("Неверный пароль");
                }
        
                // Успешная аутентификация
                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        password, // Или null, если не нужно хранить credentials
                        userDetails.getAuthorities()
                );
            }
        
            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        }

Реализация тестов для него:

        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.Test;
        import org.junit.jupiter.api.extension.ExtendWith;
        import org.mockito.InjectMocks;
        import org.mockito.Mock;
        import org.mockito.junit.jupiter.MockitoExtension;
        import org.springframework.security.authentication.BadCredentialsException;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.security.core.Authentication;
        import org.springframework.security.core.authority.SimpleGrantedAuthority;
        import org.springframework.security.core.userdetails.User;
        import org.springframework.security.core.userdetails.UserDetails;
        import org.springframework.security.core.userdetails.UserDetailsService;
        import org.springframework.security.core.userdetails.UsernameNotFoundException;
        import org.springframework.security.crypto.password.PasswordEncoder;
        
        import java.util.Collections;
        import java.util.List;
        
        import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;
        
        @ExtendWith(MockitoExtension.class)
        class CustomAuthenticationProviderTest {
        
            @Mock
            private UserDetailsService userDetailsService;
        
            @Mock
            private PasswordEncoder passwordEncoder;
        
            @InjectMocks
            private CustomAuthenticationProvider authenticationProvider;
        
            private UserDetails testUser;
            private String rawPassword = "password123";
            private String encodedPassword = "encodedPassword123";
        
            @BeforeEach
            void setUp() {
                testUser = new User("testuser", encodedPassword, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            }
        
            @Test
            void authenticate_Successful() {
                when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
                when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        
                Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", rawPassword);
                Authentication result = authenticationProvider.authenticate(authentication);
        
                assertNotNull(result);
                assertTrue(result.isAuthenticated());
                assertEquals("testuser", result.getName());
                assertEquals(1, result.getAuthorities().size());
                assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        
                verify(userDetailsService, times(1)).loadUserByUsername("testuser");
                verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
            }
        
            @Test
            void authenticate_UserNotFound() {
                when(userDetailsService.loadUserByUsername("nonexistent")).thenThrow(new UsernameNotFoundException("User not found"));
        
                Authentication authentication = new UsernamePasswordAuthenticationToken("nonexistent", rawPassword);
        
                BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> {
                    authenticationProvider.authenticate(authentication);
                });
        
                assertEquals("Пользователь не найден", thrown.getMessage());
                verify(userDetailsService, times(1)).loadUserByUsername("nonexistent");
                verify(passwordEncoder, never()).matches(anyString(), anyString()); // Пароль не должен проверяться
            }
        
            @Test
            void authenticate_WrongPassword() {
                when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
                when(passwordEncoder.matches("wrongpassword", encodedPassword)).thenReturn(false);
        
                Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "wrongpassword");
        
                BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> {
                    authenticationProvider.authenticate(authentication);
                });
        
                assertEquals("Неверный пароль", thrown.getMessage());
                verify(userDetailsService, times(1)).loadUserByUsername("testuser");
                verify(passwordEncoder, times(1)).matches("wrongpassword", encodedPassword);
            }
        
            @Test
            void supports_UsernamePasswordAuthenticationToken() {
                assertTrue(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
            }
        
            @Test
            void supports_OtherAuthenticationToken() {
                // Пример другого токена, который не должен поддерживаться
                assertFalse(authenticationProvider.supports(org.springframework.security.authentication.AnonymousAuthenticationToken.class));
            }
        }
________________________________________________________________________________________________________________________
**2. Интеграционное тестирование (Integration Testing)**

На этом уровне мы запускаем часть или весь контекст Spring, чтобы убедиться, что наш AuthenticationProvider правильно 
интегрирован с остальной частью Spring Security.
**Ключевые идеи:**
- **@SpringBootTest:** Используем эту аннотацию для загрузки контекста приложения.
- **@Autowired:** Внедряем **AuthenticationManager** или напрямую наш **AuthenticationProvider**.
- **Используем реальные зависимости:** Позволяем Spring-у управлять зависимостями, чтобы проверить, как они взаимодействуют.

**Пример интеграционного теста:**
- Тестовая конфигурация безопасности:

        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.authentication.AuthenticationProvider;
        import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
        import org.springframework.security.core.userdetails.UserDetailsService;
        import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
        import org.springframework.security.crypto.password.PasswordEncoder;
        import org.springframework.security.web.SecurityFilterChain;
        
        @Configuration
        @EnableWebSecurity
        public class TestSecurityConfig {
        
            private final AuthenticationProvider customAuthenticationProvider;
            private final UserDetailsService userDetailsService;
        
            public TestSecurityConfig(AuthenticationProvider customAuthenticationProvider, UserDetailsService userDetailsService) {
                this.customAuthenticationProvider = customAuthenticationProvider;
                this.userDetailsService = userDetailsService;
            }
        
            @Bean
            public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
            }
        
            @Bean
            public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                        .formLogin(form -> form.permitAll())
                        .logout(logout -> logout.permitAll());

                return http.build();
            }
        
            // Для демонстрации, в реальном приложении UserDetailsService будет из БД
            @Bean
            public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
                return username -> {
                    if ("testuser".equals(username)) {
                        return org.springframework.security.core.userdetails.User.withUsername("testuser")
                                .password(passwordEncoder.encode("password123"))
                                .roles("USER")
                                .build();
                    }
                    throw new UsernameNotFoundException("User not found: " + username);
                };
            }
        }

- Тест для CustomAuthenticationProvider с предложенной выше конфигурацией:



        import org.junit.jupiter.api.Test;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.boot.test.context.SpringBootTest;
        import org.springframework.context.annotation.Import;
        import org.springframework.security.authentication.AuthenticationManager;
        import org.springframework.security.authentication.BadCredentialsException;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.security.core.Authentication;
        import org.springframework.security.core.AuthenticationException;
        import org.springframework.test.context.ActiveProfiles;
        
        import static org.junit.jupiter.api.Assertions.*;
        
        @SpringBootTest
        @Import(TestSecurityConfig.class) // Импортируем нашу тестовую конфигурацию безопасности
        @ActiveProfiles("test") // Можете использовать профиль для тестовых настроек
        class CustomAuthenticationProviderIntegrationTest {
        
            @Autowired
            private AuthenticationManager authenticationManager;
        
            @Test
            void authenticate_Successful_Integration() {
                Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password123");
                Authentication authenticated = authenticationManager.authenticate(authentication);
        
                assertNotNull(authenticated);
                assertTrue(authenticated.isAuthenticated());
                assertEquals("testuser", authenticated.getName());
                assertTrue(authenticated.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            }
        
            @Test
            void authenticate_WrongPassword_Integration() {
                Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "wrongpassword");
        
                BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> {
                    authenticationManager.authenticate(authentication);
                });
        
                assertEquals("Неверный пароль", thrown.getMessage()); // Сообщение из CustomAuthenticationProvider
            }
        
            @Test
            void authenticate_UserNotFound_Integration() {
                Authentication authentication = new UsernamePasswordAuthenticationToken("non_existent", "password123");
        
                BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> {
                    authenticationManager.authenticate(authentication);
                });
        
                assertEquals("Пользователь не найден", thrown.getMessage()); // Сообщение из CustomAuthenticationProvider
            }
        }
________________________________________________________________________________________________________________________
**3. Тестирование контроллеров (Controller Testing)**
   
Иногда полезно протестировать поток аутентификации от начала до конца, включая взаимодействие с контроллером. Мы можем 
использовать MockMvc для имитации HTTP-запросов.
**Ключевые идеи:**
- **@WebMvcTest:** Для тестирования контроллеров. Он загружает только веб-слой приложения.
- **@MockBean:** "Мокаем" сервисы, от которых зависит наш контроллер, но не "мокаем" **AuthenticationManager** или 
**AuthenticationProvider**, чтобы проверить реальный поток аутентификации.
- **@WithMockUser или SecurityMockMvcRequestPostProcessors.user():** Используем для имитации аутентифицированных 
пользователей, если нужно протестировать доступ к защищенным ресурсам.

**Пример тестирования контроллера:**
- Тестовый контроллер (для примера):

        import org.springframework.security.access.prepost.PreAuthorize;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RestController;
        
        @RestController
        public class TestController {
        
            @GetMapping("/public")
            public String publicAccess() {
                return "Public content";
            }
        
            @GetMapping("/private")
            @PreAuthorize("isAuthenticated()")
            public String privateAccess() {
                return "Private content for authenticated users";
            }
        }

Интеграционный тест приведенного выше контроллера:

        import org.junit.jupiter.api.Test;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
        import org.springframework.boot.test.context.SpringBootTest;
        import org.springframework.context.annotation.Import;
        import org.springframework.security.test.context.support.WithMockUser;
        import org.springframework.test.web.servlet.MockMvc;
        
        import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
        import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
        import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
        
        @SpringBootTest
        @AutoConfigureMockMvc
        @Import(TestSecurityConfig.class)
        class ControllerAuthenticationTest {
        
            @Autowired
            private MockMvc mockMvc;
        
            @Test
            void loginWithValidCredentials_ShouldSucceed() throws Exception {
                mockMvc.perform(formLogin("/login").user("testuser").password("password123"))
                        .andExpect(authenticated().withUsername("testuser"));
            }
        
            @Test
            void loginWithInvalidCredentials_ShouldFail() throws Exception {
                mockMvc.perform(formLogin("/login").user("testuser").password("wrongpassword"))
                        .andExpect(unauthenticated());
            }
        
            @Test
            void accessPrivateEndpoint_WhenAuthenticated_ShouldSucceed() throws Exception {
                mockMvc.perform(formLogin("/login").user("testuser").password("password123")); // Аутентифицируем пользователя
        
                mockMvc.perform(get("/private"))
                        .andExpect(status().isOk());
            }
        
            @Test
            void accessPrivateEndpoint_WhenUnauthenticated_ShouldRedirectToLogin() throws Exception {
                mockMvc.perform(get("/private"))
                        .andExpect(status().is3xxRedirection()); // Перенаправление на страницу логина
            }
        
            @Test
            void accessPublicEndpoint_AlwaysAllowed() throws Exception {
                mockMvc.perform(get("/public"))
                        .andExpect(status().isOk());
            }
        }
________________________________________________________________________________________________________________________
**Рекомендации по тестированию:**

- **Начинаем с юнит-тестов:** Они быстрые и изолированные, что позволяет быстро находить и исправлять ошибки в логике AuthenticationProvider.
- **Добавляем интеграционные тесты:** Убеждаемся, что наш AuthenticationProvider правильно работает в контексте Spring Security и взаимодействует с другими компонентами.
- **Тестируем контроллеры:** Если аутентификация является ключевым аспектом пользовательского потока, проверяем, как она ведет себя на уровне HTTP-запросов.
- **Покрываем тестами успешные и неудачные сценарии:** Тестируем, как успешную аутентификацию, так и различные сценарии ошибок (неправильный пароль, пользователь не найден, заблокированный аккаунт и т.д.).
- **Используем Mockito:** Для "заглушения" зависимостей в unit-тестах.
- **Используем Spring Security Test:** Библиотека spring-security-test предоставляет удобные утилиты для тестирования Spring Security, такие как **formLogin()**, **user()** и аннотации типа **@WithMockUser**.
________________________________________________________________________________________________________________________
Подход к выбору вариантов тестирования - CustomAuthProviderTestLevel.