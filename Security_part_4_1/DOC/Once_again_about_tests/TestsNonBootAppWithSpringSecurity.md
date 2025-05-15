Тестирование **Spring Security в non-Boot** приложении требует ручной настройки контекста и моков. Разберём ключевые 
сценарии: 
- тесты контроллеров с аутентификацией; 
- интеграционные тесты с защищёнными endpoint-ами; 
- unit-тесты сервисов;
________________________________________________________________________________________________________________________
#### 1. Тестирование контроллеров с MockMvc и Security:
Для тестов MVC с Security нужно:
- Настроить **SecurityConfig** в тестовом контексте.
- Использовать **@WithMockUser** для имитации аутентификации.

Пример кода:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = {WebConfig.class,
                                         SecurityConfig.class  // Наш конфиг Spring Security
        })
        @WebAppConfiguration
        public class SecureControllerTest {
        
            @Autowired
            private WebApplicationContext webAppContext;
        
            private MockMvc mockMvc;
        
            @Before
            public void setup() {
                mockMvc = MockMvcBuilders
                    .webAppContextSetup(webAppContext)
                    .apply(springSecurity())  // Включаем Security для MockMvc
                    .build();
            }
        
            // Тест с аутентифицированным пользователем
            @Test
            @WithMockUser(roles = "USER")
            public void whenAuthenticated_thenAccessSecureEndpoint() throws Exception {
                mockMvc.perform(get("/api/secure"))
                       .andExpect(status().isOk());
            }
        
            // Тест без аутентификации
            @Test
            public void whenAnonymous_thenDenyAccess() throws Exception {
                mockMvc.perform(get("/api/secure"))
                       .andExpect(status().isForbidden());
            }
        }
________________________________________________________________________________________________________________________
#### 2. Тестирование ролей и прав доступа:
Проверка разных ролей через @WithMockUser:

        @Test
        @WithMockUser(roles = "ADMIN")
        public void whenAdmin_thenAccessAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/admin"))
                   .andExpect(status().isOk());
        }
        
        @Test
        @WithMockUser(roles = "USER")
        public void whenUser_thenDenyAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/admin"))
                   .andExpect(status().isForbidden());
        }
________________________________________________________________________________________________________________________
#### 3. Интеграционные тесты с реальным SecurityContext:
Если нужно протестировать полную цепочку Security (например, кастомные UserDetailsService или фильтры):

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = {AppConfig.class,
                                         SecurityConfig.class,
                                         TestSecurityConfig.class  // Тестовая конфигурация с in-memory UserDetailsService
        })
        @Transactional
        public class FullSecurityIntegrationTest {
        
            @Autowired
            private FilterChainProxy springSecurityFilterChain;
        
            @Test
            @WithUserDetails("admin@test.com")  // Пользователь из тестового UserDetailsService
            public void whenAdminLogin_thenSuccess() {
                // Тест с реальным SecurityContext
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                assertTrue(auth.isAuthenticated());
                assertEquals("admin@test.com", auth.getName());
            }
        }

Где **TestSecurityConfig** может выглядеть так:

        @Configuration
        @Import(SecurityConfig.class)
        public class TestSecurityConfig {
        
            @Bean
            public UserDetailsService userDetailsService() {
                UserDetails admin = User.builder()
                    .username("admin@test.com")
                    .password("{noop}admin123")  // {noop} — кодировка "plaintext" для тестов
                    .roles("ADMIN")
                    .build();
                return new InMemoryUserDetailsManager(admin);
            }
        }
________________________________________________________________________________________________________________________
#### 4. Тестирование кастомных AuthenticationProvider:
Если у нас свой провайдер аутентификации, то:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = {SecurityConfig.class,
                                         CustomAuthProvider.class})
        public class CustomAuthProviderTest {
        
            @Autowired
            private AuthenticationProvider authProvider;
        
            @Test
            public void whenValidCredentials_thenAuthenticate() {
                UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken("user", "password");
                
                Authentication authResult = authProvider.authenticate(authRequest);
                assertTrue(authResult.isAuthenticated());
            }
        }
________________________________________________________________________________________________________________________
#### 5. Тестирование CSRF и CORS:
Проверка защитных механизмов через MockMvc:

        @Test
        @WithMockUser
        public void whenCsrfEnabled_thenPostFailsWithoutToken() throws Exception {
            mockMvc.perform(post("/api/update")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{}"))
                   .andExpect(status().isForbidden());  // CSRF токен отсутствует
        }
        
        @Test
        @WithMockUser
        public void whenValidCsrfToken_thenSuccess() throws Exception {
            mockMvc.perform(post("/api/update")
                   .with(csrf())  // Добавляет CSRF токен
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{}"))
                   .andExpect(status().isOk());
        }
________________________________________________________________________________________________________________________
#### 6. Тестирование Security на уровне методов (@PreAuthorize):
Для проверки аннотаций вроде @PreAuthorize на сервисах:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = {SecurityConfig.class,
                                         UserService.class})
        @EnableGlobalMethodSecurity(prePostEnabled = true)  // Включаем проверку аннотаций
        public class MethodSecurityTest {
        
            @Autowired
            private UserService userService;
        
            @Test(expected = AccessDeniedException.class)
            @WithMockUser(roles = "USER")
            public void whenUserTriesAdminMethod_thenDeny() {
                userService.deleteUser(1L);  // Метод с @PreAuthorize("hasRole('ADMIN')")
            }
        }
________________________________________________________________________________________________________________________
#### 7. Тестирование OAuth2 (без Boot):
Если используется кастомный OAuth2:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = {OAuth2Config.class,
                                         ResourceServerConfig.class})
        public class OAuth2Test {
        
            @Test
            @WithMockOAuth2Client("client-id")  // Кастомная аннотация
            public void whenOAuth2TokenValid_thenAccessResource() throws Exception {
                mockMvc.perform(get("/api/resource")
                       .header("Authorization", "Bearer mock-token"))
                       .andExpect(status().isOk());
            }
        }

Где: **@WithMockOAuth2Client** можно реализовать через **WithSecurityContextFactory**.
________________________________________________________________________________________________________________________
Ключевые моменты:
1. Для **MockMvc**:
   - Используем **springSecurity()** в настройке.
   - Аннотации **@WithMockUser**, **@WithUserDetails**.
2. Для интеграционных тестов:
   - Настраиваем тестовый **UserDetailsService**.
   - Проверяем **SecurityContextHolder**.
3. Для Security на уровне методов:
   - Включаем **@EnableGlobalMethodSecurity** в тестах.
4. Кастомные провайдеры/фильтры:
   - Тестируем их изолированно или в контексте.

________________________________________________________________________________________________________________________
**Важно:** 
В отличие от Spring Boot, non Boot проектах нет автоматической настройки Security для тестов — всё конфигурируется вручную 
через **@ContextConfiguration**.
________________________________________________________________________________________________________________________
Примеры тестов в non Boot проекте - [test](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_3/src/test) или 
см. проекты этого "справочника" с пометкой (non Boot) [в оглавлении](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide).