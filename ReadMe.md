### Spring Security Tutorial (Lessons).

________________________________________________________________________________________________________________________

- [Security_part_1 - First simple Spring Boot Security App - настройки по умолчанию (Spring Boot) (Maven).](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_1)
________________________________________________________________________________________________________________________

- [Security_part_2_1 - Частичное ограничение доступа к ресурсам приложения (Light legacy) (Maven).](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_2_1)
- [Security_part_2_2 - Частичное ограничение доступа к ресурсам приложения (свежая кодовая база) (Gradle).](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_2_2)
________________________________________________________________________________________________________________________

- [Security_part_3_1 - Использование нескольких пользователей при доступе к приложению (in-memory аутентификация) (Light legacy) (Maven).](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_1)
- [Security_part_3_2 - Получение данным из БД и настройка авторизации краткий пример (Light legacy) (Maven).](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_2)
  - [Настройка авторизации.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_2/ReadMe.md#%D0%BD%D0%B0%D1%81%D1%82%D1%80%D0%BE%D0%B9%D0%BA%D0%B0-%D0%B0%D0%B2%D1%82%D0%BE%D1%80%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D0%B8)
  - [Настройка доступа (роли, разрешения).](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_2/ReadMe.md#%D0%BD%D0%B0%D1%81%D1%82%D1%80%D0%BE%D0%B9%D0%BA%D0%B0-%D0%B4%D0%BE%D1%81%D1%82%D1%83%D0%BF%D0%B0-%D1%80%D0%BE%D0%BB%D0%B8-%D1%80%D0%B0%D0%B7%D1%80%D0%B5%D1%88%D0%B5%D0%BD%D0%B8%D1%8F)
- Security_part_3_3 - Получение данным из БД и настройка авторизации краткий пример (свежая кодовая база) (non Boot) (Gradle).
- Security_part_3_4 - Получение данным из БД и настройка авторизации краткий пример (свежая кодовая база) (Spring Boot) (Gradle).
________________________________________________________________________________________________________________________

- Security_part_4_1 - Пользовательская аутентификация в Spring Security (кастомный UserDetailsService и UserDetails) (Spring Boot).
- Security_part_4_2 - Пользовательская аутентификация в Spring Security (кастомный UserDetailsService и UserDetails) (non Boot).
________________________________________________________________________________________________________________________

- Security_part_5_1 - Пользовательский AuthenticationProvider (Spring Boot).
- Security_part_5_2 - Пользовательский AuthenticationProvider, UserDetailsService и UserDetails (Spring Boot).
________________________________________________________________________________________________________________________

- Security_part_6_1 - Remember-Me Authentication (Spring Boot).
  - Hash Based токен и проверка подлинности - теория.
  - Настройка Remember-Me аутентификации.
________________________________________________________________________________________________________________________

- Security_part_7_1 - Настройка CORS, CSRF и еще раз немного о RememberMe (Spring Boot).
  - Настройка Security CORS в Spring приложении.
  - Генерация Security CSRF-токена в Spring приложении (самописные формы аутентификации и регистрации).
  - Еще раз о настройке Remember-Me Authentication и управление сессиями.
  - Важные моменты.
  - Особенности настройки отображения.
- Security_part_7_2 - Немного об отличиях Role от Authority (non Boot).
________________________________________________________________________________________________________________________

- Security_part_8_1 - Применение пользовательских фильтров в FilterChain (методы addFilterBefore(), addFilterAfter() и addFilterAt()) (Spring Boot).
  - Часть 1. - Реализация самописных фильтров (filters) в Spring Security (теория).
  - Ключевые понятия (определения) и концепции.
  - Реализация пользовательского (самописного, custom) фильтра и добавление его перед уже используемым.
  - Добавление фильтра после уже существующего.
  - Замена существующего фильтра custom фильтром.
  - Часть 2. - Реализация самописных фильтров (filters) в Spring Security (практика).
________________________________________________________________________________________________________________________

- Security_part_9_1 - Применение JWT токена (начало) (Spring Boot).
  - Часть 1. - JWT Token (теория).
  - JWT токен vs Simple Hash-Based Remember-Me токен.
  - Структура JWT.
  - Как проверяется подлинность токена?
  - В каком заголовке передается JWT-токен?
  - Часть 2. - Пример приложения с JWT-токеном (девиантный подход).
  - Логика работы приложения.
- Security_part_9_2 - Применение JWT токена (продолжение) (Spring Boot).
  - Часть 3. - Пример приложения с JWT-токеном (девиантный подход, продолжение и окончание). 
- Security_part_9_3 - Применение JWT токена (окончание) (non Boot).
  - Часть 4. - Пример приложения с JWT-токеном (классический подход).
  - Интересный момент в работе nonBoot Spring приложения.
________________________________________________________________________________________________________________________

- Security_part_10_1 - Применение Method Level Security (защита и фильтрация на уровне методов) (non Boot).
  - Часть 1. - Защита на уровне методов — аннотация @PreAuthorize (теория).
  - @PreAuthorize (практика).
  - Часть 2. - Защита на уровне методов — аннотация @PostAuthorize (теория).
  - @PostAuthorize (практика).
  - Несколько аннотаций безопасности для одного метода (дополнения к теории).
  - Часть 3. - Защита на уровне методов — аннотации @PreFilter и @PostFilter(теория).
  - Особенность работы аннотаций @PreFilter, PostFilter и их производительность в фильтрации больших списков.
  - @PostFilter и @PreFilter (практика).
  - Часть 4. - Аннотация @EnableMethodSecurity (теория).
  - Часть 5. - Аннотация @Secured (теория).
  - @Secured (практика).
  - Часть 6. - Аннотация @RolesAllowed (теория).
________________________________________________________________________________________________________________________

- Security_part_11_1 - Применение OAuth2 в защите приложений (начало) (Spring Boot).
  - Часть 1. - OAuth2 (Google as Authorization service).
  - Часть 2. - Согласование данных из OAuth2.0 сервиса с нашим web-приложением.
  - Часть 3. - OAuth2 (GitHub as Authorization service).
- Security_part_11_2 - Применение OAuth2 в защите приложений (продолжение) (Spring Boot).
  - Часть 4. - Применение KeyCloak в качестве OAuth2 сервиса авторизации.
- Security_part_11_3 - Применение OAuth2 в защите сервиса ресурсов (окончание) (non Boot).
  - Часть 5. - Применение KeyCloak в качестве OAuth2 сервиса авторизации для Resource service.
________________________________________________________________________________________________________________________
Док. (ссылки) для изучения:
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) ;
- [Spring Framework 6.1.5 Documentation](https://spring.io/projects/spring-framework) ;
- [Spring Framework 3.2.x Reference Documentation](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/index.html) ;
- [Getting Started Guides](https://spring.io/guides) ;
- [Developing with Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html) ;
- [Документация по Spring Boot (архив)](https://docs.spring.io/spring-boot/docs/) ;
________________________________________________________________________________________________________________________
См. официальные [Guides](https://spring.io/guides):
- [Getting Started Guides](https://spring.io/guides) - Эти руководства, рассчитанные на 15–30 минут, содержат быстрые
  практические инструкции по созданию «Hello World» для любой задачи разработки с помощью Spring. В большинстве случаев
  единственными необходимыми требованиями являются JDK и текстовый редактор.
- [Topical Guides](https://spring.io/guides#topicals) - Тематические руководства предназначенные для прочтения и
  понимания за час или меньше, содержит более широкий или субъективный контент, чем руководство по началу работы.
- [Tutorials](https://spring.io/guides#tutorials) - Эти учебники, рассчитанные на 2–3 часа, обеспечивают более глубокое
  контекстное изучение тем разработки корпоративных приложений, что позволяет вам подготовиться к внедрению реальных
  решений.
________________________________________________________________________________________________________________________
- [Spring Projects на GitHub](https://github.com/spring-projects) ;
________________________________________________________________________________________________________________________