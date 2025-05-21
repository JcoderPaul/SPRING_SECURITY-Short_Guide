Spring Security предоставляет интерфейс AuthenticationProvider как ключевой компонент процесса аутентификации. По сути, 
AuthenticationProvider отвечает за фактическую проверку учетных данных пользователя и создание объекта Authentication, 
представляющего успешно аутентифицированного пользователя.

Каждый такой провайдер можно считать конкретным механизмом проверки подлинности. Т.к. Spring Security может иметь 
несколько AuthenticationProvider в цепочке, каждый из которых отвечает за определенный способ аутентификации (например, 
по имени пользователя и паролю, через OAuth2, SAML и т.д.).

**Основные обязанности AuthenticationProvider:**
1. **Проверка подлинности:** Получает объект **Authentication** (обычно содержащий имя пользователя и учетные данные, например,
пароль) и пытается аутентифицировать пользователя на основе этих данных.
2. **Создание Authentication:** В случае успешной аутентификации создает заполненный объект **Authentication**, содержащий 
информацию о Principal (аутентифицированном пользователе), Authorities (его ролях и разрешениях) и другие детали.
3. **Обработка ошибок:** Если аутентификация не удалась, выбрасывает исключение **AuthenticationException** с информацией 
о причине неудачи. 
4. **Поддержка Authentication:** Определяет, поддерживает ли данный **AuthenticationProvider** переданный ему тип объекта 
**Authentication**. Это позволяет Spring Security направлять запрос на аутентификацию к соответствующему провайдеру.

**Как использовать AuthenticationProvider:**
1. **Реализация интерфейса:** Нам нужно создать класс, который реализует интерфейс [AuthenticationProvider](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/AuthenticationProvider.html). 
Этот интерфейс содержит три метода:
    - **Authentication authenticate(Authentication authentication) throws AuthenticationException;:** Этот метод является 
основной логикой аутентификации. Он принимает объект Authentication в качестве аргумента и должен попытаться аутентифицировать 
пользователя. Если аутентификация успешна, он должен вернуть новый, полностью аутентифицированный объект Authentication. 
Если нет, он должен выбросить AuthenticationException.
    - **boolean supports(Class<?> authentication);:** Этот метод сообщает Spring Security, какие типы объектов Authentication 
поддерживает данный провайдер. Например, провайдер, проверяющий имя пользователя и пароль, будет поддерживать UsernamePasswordAuthenticationToken.
    - **default boolean supportsAuthorization(Authorization authorization) { return false; }:** Этот метод используется для
поддержки авторизации, начиная с [Spring Security 6.2.](https://docs.spring.io/spring-security/reference/index.html) Обычно возвращает false для провайдеров аутентификации.

2. **Регистрация в Spring Security:** После того как мы реализовали свой **Custom_AuthenticationProvider**, нам необходимо зарегистрировать 
его в конфигурации Spring Security. Обычно это делается в классе, аннотированном **@Configuration** и **@EnableWebSecurity**. Мы можем 
добавить свои **AuthenticationProvider** в **AuthenticationManagerBuilder**. Т.е. можно выделить его как **@Component**, см. [CustomAuthProvider.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_5_1/src/main/java/me/oldboy/config/auth_provider/CustomAuthProvider.java) и
указать в качестве объекта @ComponentScan("...component folder...") в соответствующем файле конфигурации.

**Пример простой реализации AuthenticationProvider (для демонстрации):**

        import org.springframework.security.authentication.AuthenticationProvider;
        import org.springframework.security.authentication.BadCredentialsException;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.security.core.Authentication;
        import org.springframework.security.core.AuthenticationException;
        import org.springframework.security.core.authority.SimpleGrantedAuthority;
        import org.springframework.stereotype.Component;
        
        import java.util.Collections;
        
        @Component
        public class CustomAuthenticationProvider implements AuthenticationProvider {
        
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String name = authentication.getName();
                String password = authentication.getCredentials().toString();
        
                /* В реальном приложении здесь будет логика проверки учетных данных, например, обращение к базе данных. */
                if ("user".equals(name) && "password".equals(password)) {
                    /* Создаем объект Authentication с информацией о пользователе и его ролях */
                    return new UsernamePasswordAuthenticationToken(
                            name,
                            password,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                } else {
                    throw new BadCredentialsException("Неверные имя пользователя или пароль");
                }
            }
        
            @Override
            public boolean supports(Class<?> authentication) {
                /* Указываем, что этот провайдер поддерживает UsernamePasswordAuthenticationToken */
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        }

**Конфигурация Spring Security для использования этого провайдера:**


        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.authentication.AuthenticationManager;
        import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.web.SecurityFilterChain;
        
        @Configuration
        public class SecurityConfig {
        
            @Autowired
            private CustomAuthenticationProvider customAuthenticationProvider;
        
            @Bean
            public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
                auth.authenticationProvider(customAuthenticationProvider);
                return auth.build();
            }
        
            @Bean
            public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                        .authorizeHttpRequests((requests) -> requests.anyRequest().authenticated())
                        .formLogin((form) -> form.permitAll())
                        .logout((logout) -> logout.permitAll());
        
                return http.build();
            }
        }

**В этом примере:**

- **CustomAuthenticationProvider** проверяет, что имя пользователя "user" и пароль "password". В реальном приложении эта 
логика будет взаимодействовать с хранилищем пользователей (базой данных, LDAP и т.д.).
- Метод **supports()** указывает, что этот провайдер обрабатывает UsernamePasswordAuthenticationToken, который обычно 
используется при аутентификации через форму логина.
- В конфигурации **SecurityConfig** мы внедряем наш **CustomAuthenticationProvider** и добавляем его в **AuthenticationManagerBuilder**. 
Это говорит Spring Security использовать наш провайдер в процессе аутентификации.

**Когда нам может понадобиться свой AuthenticationProvider:**
- **Нестандартные источники аутентификации:** Если наши пользователи хранятся не в традиционной базе данных, а, например, в каком-то внешнем сервисе или формате.
- **Нестандартные способы аутентификации:** Если мы хотим реализовать аутентификацию с использованием токенов, смарт-карт, биометрии или других нетрадиционных методов.
- **Интеграция с устаревшими системами:** Если нам нужно интегрировать Spring Security с существующей системой аутентификации, которая не соответствует стандартным механизмам.
- **Кастомизация процесса аутентификации:** Если нам требуется выполнить какие-либо дополнительные действия во время процесса аутентификации, например, аудит или обогащение информации о пользователе.

Использование **AuthenticationProvider** позволяет гибко настраивать процесс аутентификации в Spring Security и интегрировать 
его с различными системами и механизмами безопасности.