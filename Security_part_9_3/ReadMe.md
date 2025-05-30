### Simple Spring App with Security (Part 9_3) - применение JWT токена (окончание).

- Spring 6.2.2
- Spring Security 6.4.2
- Java 17
- Gradle
________________________________________________________________________________________________________________________
### Часть 4. - Пример приложения с JWT-токеном (классический подход).

Как намекнул нам один хороший друг: "JWT - это просто транспорт, особенно никакой разницы нет, что там хранить и как его 
отдавать, главное - пароль туда не класть и уже хорошо!" Просто и доходчиво, если учесть наш первый опыт работы с данным 
методом передачи средств аутентификации (идентификации) - [BankAccountSimulator](https://github.com/JcoderPaul/BankAccountSimulator_Task/tree/master/src/main/java/prod/oldboy/security), 
в котором исходный вариант [JWTGenerator-a](https://github.com/JcoderPaul/BankAccountSimulator_Task/blob/master/src/main/java/prod/oldboy/security/JwtTokenGenerator.java) использовал пароль в качестве 'клейма'.
Похожий подход применялся в [Evolution_app_development](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/security). 

Однако мы не хотели повторять уже пройденное и решили применить немного другой подход к работе с JWT token-ом. Он использовался в
Security_part_9_1 (дополнительное изучение session и вариантов перенаправления запросов) и Security_part_9_2 (дополнительное изучение cookie) - хотя принципы 
не изменились - сервер генерирует token и отдает его клиенту (тем или иным способом), клиент при каждом запросе возвращает
токен (обычно в header-e), сервер проверяет валидность полученного token-a и решает давать ли доступ или нет к защищенному 
контенту.

В данном примере мы используем канонический подход: сервер ничего не хранить - только генерирует и валидирует token при запросах.
Хотя и тут не обошлось без хитрых вывертов. Кратко выглядит это следующим образом:

- Шаг 0. - новый клиент может обратиться к сервису на endpoint - http://localhost:8080/regClient, и зарегистрироваться, передав в теле запроса JSON объект вида - ClientCreateDto (если все прошло нормально, учетные данные оригинальные и БД не бросила исключение о дублировании, можно пройти процедуру аутентификации);
- Шаг 1. - клиент для получения действующего token-a обращается к сервису на endpoint - http://localhost:8080/loginClient, с POST запросом и JSON объектом в теле вида - ClientAuthRequest, и получает ответ в виде JSON - ClientAuthResponse (если все верно клиент продолжает работу или получает ошибку аутентификации);
- Шаг 2. - поскольку у нас активна система безопасности, клиент при каждом последующем обращении к защищенному endpoint-у приложения (например из Postman) обязан предоставлять полученный ранее JWT token в header-e запроса 'Authorization';

Если же смотреть с 'высоты птичьего полета', то у нас есть шесть основных блока отвечающих за работу системы безопасности 
в этом приложении:
- AppSecurityConfig.java - файл где прописаны основные условия и настройки безопасности (предоставляется Spring-ом);
- UserPassValidatorAndAfterLogoutFilter.java - первоначальный валидатор по паролю и логину, фильтр в цепи безопасности, и он не активен в случае когда пользователь проходит процедуру регистрации;
- JwtTokenGeneratorAndAfterFilter.java - генератор JWT token-a, фильтр в цепи безопасности и он активен только в случае прохождения клиентом процедуры аутентификации (обращении к '/loginClient'), он отвечает за генерацию token-a и возврат его в response;
- JwtTokenValidatorAndBeforeFilter.java - валидатор JWT token-a, фильтр в цепи безопасности и он активен во всех случаях, кроме случаев обращения к '/regClient' и '/loginClient', т.е. именно он, как и ранее парсит полученный token и задает объект Authentication;

А теперь самые интересные две части - request wrappers (обертки запросов), если напрямую парсить структуру request-a классическим 
способом, например, как мы делали это ранее через взятие входящего потока из запроса [UserLoginServlet](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/java/me/oldboy/servlets/user/UserLoginServlet.java):

    JwtAuthRequest jwtAuthRequest = objectMapper.readValue(req.getInputStream(), JwtAuthRequest.class);

То получим I/O ошибку от Catalin-ы о том, что она не может прочитать входящий запрос - мы же сами, используя, выше описанный 
подход закрыли входящий поток прочитав его. И где же мы замутили такую глупость, а в фильтре отвечающем за обработку запроса к 
'/loginClient' endpoint-у - UserPassValidatorAndAfterLogoutFilter:

    ClientAuthRequest clientAuthRequest = objectMapper.readValue(cachedBodyHttpServletRequest.getInputStream(), ClientAuthRequest.class);

Нам ведь нужно идентифицировать и провалидировать полученный от клиента логин (username) и пароль (password) и мы хотим 
сделать это именно в фильтре. Вот тут нам на помощь приходит вариант с кешированием входящего потока, теперь мы можем не 
нарушая гармонии и не рискуя вызвать ошибку со стороны Catalin-ы распарсить request и получить учетные данные для 
аутентификации. За это отвечают два класса:

- CachedBodyHttpServletRequest - кэшируем тело запроса, расширяет HttpServletRequestWrapper;
- CachedBodyServletInputStream - кэшируем входящий поток, расширяет ServletInputStream;

И так, что же происходит (этап регистрации пропустим):
- Шаг 1. - Клиент обращается к endpoint-у "/loginClient" - в работу вступает фильтр UserPassValidatorAndAfterLogoutFilter 
(стандартные фильтры Spring Security упоминать не будем, они работают, как им и положено), в котором мы кэшируем request, 
и далее извлекаем из него ClientAuthRequest вида:

      {
        "username":"admin@test.com",
        "password":"1234"
      }

Проводим необходимые проверки валидности данных, затем сверяем с данными из БД, если все верно создаем объект Authentication
и помещаем его в SecurityContext.

- Шаг 2. - Мы все еще находимся в цепочки фильтров security chain и следующим нашим фильтром будет JwtTokenGeneratorAndAfterFilter, 
который извлекает объект Authentication из контекста безопасности, далее на его базе фильтр создает JWT token и возвращает его
клиенту в заголовке response "Authorization". При этом метод *.loginClient() класса ClientController возвращает JSON объект 
вида - ClientAuthResponse:

        {
          "id": 1,
          "clientLogin": "admin@test.com",
          "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFkbWluQHRlc3QuY29tIiwiYXV0aG9yaXRpZXMiOiJSRUFELE1PUkUgQkFEIEFDVElPTixBRE1JTiIsImlhdCI6MTc0NDEyMDgxMiwiZXhwIjoxNzQ0NDIwODEyfQ.6HBolJbqhOwArNeDDKktHRvI26UqjRAs2Cb6sLUkLw0"
        }

Теперь клиент может использовать в работе, как данные из заголовка "Authorization", так и тело ответа.

- Шаг 3. - Теперь при обращении к любому защищенному endpoint-у клиент передает JWT token полученный на шаге 2, т.е. при 
аутентификации. Тут в работу включается наш третий фильтр не относящийся к стандартным фильтрам Spring-a - JwtTokenValidatorAndBeforeFilter,
который читает содержимое заголовка (header-a) "Authorization", далее парсит его и получает данные упакованные на этапе 
генерации ключа. Затем помещает полученную информацию в UsernamePasswordAuthenticationToken и затем уже в контекст безопасности.
Далее в работу вступают стандартные фильтры безопасности Spring-a.

Т.е. все, как и раньше, вместе с запросом отправляем токен, если он валиден - данные доступны, нет - в доступе отказано.

На этом тему о применении JWT token-ов в Spring сервисах можно завершить.
________________________________________________________________________________________________________________________
### Интересный момент в работе nonBoot Spring приложении:

При работе со слоем repository и формированием нативных SQL запросов возник интересный "затык" в виде ошибки:

        "For queries with named parameters you need to provide names for method parameters; 
         Use @Param for query method parameters, or when on Java 8+ use the javac flag -parameters"

Оказывается в данном случае мы должны явно указывать название параметра в аннотации:

        @Query(value = "SELECT * " +
                       "FROM loans " +
                       "WHERE loans.client_id = :clientId",
               nativeQuery = true)
        Optional<List<Loan>> findAllByClientId(@Param("clientId") Long clientId);

Интересным тут является еще и тот момент, что в следующем проекте, при работе с аннотацией @PostAuthorize, которая в 
качестве параметра использует SpEL выражения мы поймали похожую ошибку и вновь пришлось использовать аннотацию @Param к
передаваемому в метод аргументу для разрешения недопонимания при проксировании.
________________________________________________________________________________________________________________________
### Key Class and Methods:

* [Class HttpSecurity](https://docs.spring.io/spring-security/site/docs/5.0.0.M5/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(javax.servlet.Filter,%20java.lang.Class))
* [Interface HttpSecurityBuilder](https://docs.spring.io/spring-security/site/docs/4.1.0.RC2/apidocs/org/springframework/security/config/annotation/web/HttpSecurityBuilder.html)

### Reference Documentation:

* [JSON Web Token (JWT)](https://www.rfc-editor.org/rfc/rfc7519.html)
* [JSON Web Token](https://en.wikipedia.org/wiki/JSON_Web_Token)

* [Spring Security Filter Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
* [CSRF](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html)
* [Cross Site Request Forgery (CSRF)](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
* [CORS Servlet Applications](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
* [CORS Reactive Applications](https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html)

* [UserDetails](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details.html)
* [UserDetailsService](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html)

* [Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/index.html)
* [AuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html)
* [JDBC Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc-datasource)
* [DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html)
* [Core Configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)
* [Username/Password Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html#publish-authentication-manager-bean)

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Reference Guide (history)](https://docs.spring.io/spring-boot/docs/)
* [Spring Security](https://spring.io/projects/spring-security)
* [Spring Security Examples](https://spring.io/projects/spring-security#samples)

### Guides:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Spring Guides](https://spring.io/guides)
* [Spring Boot Security Auto-Configuration](https://www.baeldung.com/spring-boot-security-autoconfiguration)
* [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)
* [Spring Security: Upgrading the Deprecated WebSecurityConfigurerAdapter](https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter)

### Articles (question-answer):

* [JSON Web Token Claims](https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-token-claims)
* [Пять простых шагов для понимания JSON Web Tokens (JWT)](https://habr.com/ru/articles/340146/)
* [JWT Security Best Practices](https://curity.io/resources/learn/jwt-best-practices/)

* [A Custom Filter in the Spring Security Filter Chain](https://www.baeldung.com/spring-security-custom-filter)
* [Путешествие к центру Spring Security](https://habr.com/ru/articles/724738/)
* [CSRF Protection in Spring Security](https://www.geeksforgeeks.org/csrf-protection-in-spring-security/)
* [A Guide to CSRF Protection in Spring Security](https://www.baeldung.com/spring-security-csrf)
* [Spring Security - CORS](https://www.geeksforgeeks.org/spring-security-cors/)
* [CORS with Spring](https://www.baeldung.com/spring-cors)
* [Spring Security – UserDetailsService and UserDetails with Example](https://www.geeksforgeeks.org/spring-security-userdetailsservice-and-userdetails-with-example/)
* [@Valid Annotation on Child Objects](https://www.baeldung.com/java-valid-annotation-child-objects)
* [Java Bean Validation Basics](https://www.baeldung.com/java-validation)
* [Javax validation on nested objects - not working](https://stackoverflow.com/questions/53999226/javax-validation-on-nested-objects-not-working)
* [Проверка данных — Java & Spring Validation](https://habr.com/ru/articles/424819/)
________________________________________________________________________________________________________________________