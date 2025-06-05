### Simple Spring Boot App with Security (Part 8_1) - применение пользовательских фильтров в FilterChain (методы addFilterBefore(), addFilterAfter() и addFilterAt()).

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle
________________________________________________________________________________________________________________________
### Часть 1. - Реализация самописных фильтров (filters) в Spring Security (теория).

Для перехвата неких событий в JavaEE применялись фильтры, классы реализующие интерфейс Filter, в Spring Framework 
используется концепция слушателей или перехватчиков событий - Interceptor. В текущем приложении один такой реализован -
[AuthenticationEventListener.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/config/auth_event_listener/AuthenticationEventListener.java). Однако мы с легкостью можем продолжать использовать технологию Servlet Filter-ов в нашем 
Spring приложении.

Spring Security - мощный фреймворк, который предоставляет комплексную службу безопасности для приложений Java. Одной из 
ее ключевых особенностей является возможность обработки задач безопасности с помощью цепочки фильтров ([FilterChain](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/config/AppSecurityConfig.java#L44)). Эти 
фильтры управляют аутентификацией, авторизацией, защита от различных атак и т.п. Однако каждое приложение имеет уникальные 
требования безопасности, которые могут не полностью удовлетворяться фильтрами предоставленными по умолчанию в Spring Security. 

Чтобы удовлетворить нестандартные потребности, Spring Security позволяет разработчикам реализовывать и настраивать 
пользовательские фильтры ([custom filters](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_8_1/src/main/java/me/oldboy/filters)). Ниже мы кратко рассмотрим процесс создания и интеграции пользовательских 
фильтров в Spring Security. 

У нас есть возможность добавлять дополнительные шаги проверки, регистрировать события аутентификации и даже заменять 
существующие фильтры нашими пользовательскими реализациями оных. Эта гибкость позволяет адаптировать механизмы безопасности 
к точным потребностям нашего приложения, обеспечивая соответствие нашей политике безопасности.
________________________________________________________________________________________________________________________
### Ключевые понятия (определения) и концепции.

1. Цепочка фильтров в Spring Security:
   - Spring Security использует ряд фильтров для обработки различных аспектов безопасности, таких как аутентификация и авторизация.
   - Каждый фильтр в цепочке обрабатывает запрос и ответ перед передачей их следующему фильтру, формируя цепочку обязанностей.
2. Пользовательские фильтры:
   - Пользовательские фильтры могут быть реализованы для обработки определенных (не стандартных) требований безопасности, не охваченных фильтрами по умолчанию.
   - Пользовательские фильтры создаются путем реализации интерфейса [Filter](https://jakarta.ee/specifications/servlet/5.0/apidocs/jakarta/servlet/filter) из пакета [jakarta.servlet](https://jakarta.ee/specifications/servlet/5.0/apidocs/jakarta/servlet/package-summary).
3. Добавление фильтров в цепочку:
   - Пользовательские фильтры могут быть добавлены в цепочку фильтров Spring Security в определенных позициях относительно существующих фильтров.
   - Такие методы, как [addFilterBefore()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(jakarta.servlet.Filter,java.lang.Class)), [addFilterAfter()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterAfter(jakarta.servlet.Filter,java.lang.Class)) и [addFilterAt()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterAt(jakarta.servlet.Filter,java.lang.Class)), используются для указания позиции пользовательского фильтра в цепочке.
4. Замена существующих фильтров:
   - Существующие фильтры могут быть заменены пользовательскими фильтрами для реализации альтернативной логики аутентификации или авторизации.
   - Метод addFilterAt() используется для размещения пользовательского фильтра в позиции заменяемого фильтра.
5. Реализация логики фильтра:
   - Основная логика пользовательского фильтра реализована в методе [doFilter()](https://jakarta.ee/specifications/servlet/4.0/apidocs/), где фильтр обрабатывает запрос и ответ.
   - Например, пользовательский фильтр может проверять наличие определенного заголовка или регистрировать события аутентификации.
6. Управление контекстом безопасности (Security Context):
   - Security Context содержит сведения об аутентифицированном пользователе и управляется с помощью [SecurityContextHolder](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/core/context/SecurityContextHolder.html).
   - Различные стратегии управления контекстом безопасности включают: MODE_THREADLOCAL, MODE_INHERITABLETHREADLOCAL и MODE_GLOBAL.
   - MODE_THREADLOCAL - стратегия по умолчанию, при которой каждый поток имеет свой собственный контекст безопасности (Security Context).
   - MODE_INHERITABLETHREADLOCAL - позволяет наследовать контекст безопасности дочерними потоками, что полезно для асинхронных операций.
   - MODE_GLOBAL - разделяет контекст безопасности между всеми потоками, что подходит для автономных приложений.
7. SecurityContext и управление потоками (Thread Management):
   - SecurityContext хранит данные аутентификации (authentication), к которым можно получить доступ, и которыми можно управлять в разных потоках.
   - Для самоуправляемых потоков такие инструменты, как DelegatingSecurityContextRunnable, помогают распространять SecurityContext.
________________________________________________________________________________________________________________________
### Реализация пользовательского (самописного, custom) фильтра и добавление его перед уже используемым.

- Шаг 1: Создадим класс пользовательского фильтра: 
Чтобы создать пользовательский фильтр, нам необходимо реализовать интерфейс [Filter](https://jakarta.ee/specifications/servlet/5.0/apidocs/jakarta/servlet/filter) из пакета [jakarta.servlet](https://jakarta.ee/specifications/servlet/5.0/apidocs/jakarta/servlet/package-summary). Ниже приведен 
пример простого пользовательского фильтра, который проверяет наличие пользовательского заголовка X-Custom-Header в запросе:

      import jakarta.servlet.Filter;
      import jakarta.servlet.FilterChain;
      import jakarta.servlet.FilterConfig;
      import jakarta.servlet.ServletException;
      import jakarta.servlet.ServletRequest;
      import jakarta.servlet.ServletResponse;
      import jakarta.servlet.http.HttpServletRequest;
      import jakarta.servlet.http.HttpServletResponse;
      import java.io.IOException;
      
      public class CustomHeaderFilter implements Filter {

          @Override
          public void init(FilterConfig filterConfig) throws ServletException { // Initialization logic if needed }
      
          @Override
          public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                  throws IOException, ServletException {
              HttpServletRequest httpRequest = (HttpServletRequest) request;
              HttpServletResponse httpResponse = (HttpServletResponse) response;
      
              String customHeader = httpRequest.getHeader("X-Custom-Header");
              if (customHeader == null || customHeader.isBlank()) {
                  httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  return;
              }
      
              chain.doFilter(request, response);
          }
      
          @Override
          public void destroy() { // Cleanup logic if needed }
      }

Этот фильтр проверяет, присутствует ли в запросе хэдер X-Custom-Header и не является ли он пустым. Если такового нет, 
или он пустой, то фильтр устанавливает статус ответа 400 Bad Request и завершает запрос. В названии самого фильтра нет 
намека на место его будущего размещения - оно появляется явно в момент размещения фильтра в цепочке фильтров безопасности 
Spring.

- Шаг 2: Добавим пользовательский фильтр в цепочку фильтров:
Нам необходимо добавить пользовательский фильтр в цепочку фильтров Spring Security. Это можно сделать в классе 
конфигурации с помощью объекта [HttpSecurity](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html). Вот как можно добавить CustomHeaderFilter перед 
[UsernamePasswordAuthenticationFilter](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html) (применяем метод [*.addFilterBefore()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(jakarta.servlet.Filter,java.lang.Class))):

      import org.springframework.context.annotation.Bean;
      import org.springframework.context.annotation.Configuration;
      import org.springframework.security.config.annotation.web.builders.HttpSecurity;
      import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
      import org.springframework.security.web.SecurityFilterChain;
      import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
      
      @Configuration
      @EnableWebSecurity
      public class SecurityConfig {
      
          @Bean
          public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
              http.addFilterBefore(new CustomHeaderFilter(), UsernamePasswordAuthenticationFilter.class)
                  .authorizeRequests()
                  .anyRequest().authenticated()
                  .and()
                  .httpBasic();
              
              return http.build();
          }
      }

Такая конфигурация (с применением метода [*.addFilterBefore()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(jakarta.servlet.Filter,java.lang.Class))) гарантирует, что CustomHeaderFilter будет выполнен до 
[UsernamePasswordAuthenticationFilter](https://docs.spring.io/spring-security/site/docs/4.2.6.RELEASE/apidocs/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html).

Подобный "фокус" мы провернули в другом нашем приложении [BankAccountSimulator](https://github.com/JcoderPaul/BankAccountSimulator_Task),
там у нас есть фильтр обрабатывающий JWT аутентификацию, о которой будет чуть позже и в другой части. И так, у нас там
есть [SecurityChain с методом *.addFilterBefore()](https://github.com/JcoderPaul/BankAccountSimulator_Task/blob/master/src/main/java/prod/oldboy/config/SecurityConfig.java#L52),
в который передается фильтр [JwtAuthFilter](https://github.com/JcoderPaul/BankAccountSimulator_Task/blob/master/src/main/java/prod/oldboy/security/JwtAuthFilter.java).
Вполне показательный пример.
________________________________________________________________________________________________________________________
### Добавление фильтра после уже существующего.

Чуть выше мы рассмотрели два основных шага по созданию и добавлению наших самописных фильтров в Spring Security FilterChain.
Предположим, что теперь мы хотим регистрировать все успешные попытки аутентификации используя не технологию перехватчиков, 
а внедрение фильтров в цепочку безопасности. Мы можем создать фильтр для регистрации требуемых событий и добавить его 
после UsernamePasswordAuthenticationFilter:

      import jakarta.servlet.Filter;
      import jakarta.servlet.FilterChain;
      import jakarta.servlet.FilterConfig;
      import jakarta.servlet.ServletException;
      import jakarta.servlet.ServletRequest;
      import jakarta.servlet.ServletResponse;
      import jakarta.servlet.http.HttpServletRequest;
      import jakarta.servlet.http.HttpServletResponse;
      import org.slf4j.Logger;
      import org.slf4j.LoggerFactory;
      
      import java.io.IOException;
      
      public class RegistrationLoggingFilter implements Filter {
      
          private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
      
          @Override
          public void init(FilterConfig filterConfig) throws ServletException { // Initialization logic if needed }
      
          @Override
          public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
              HttpServletRequest httpRequest = (HttpServletRequest) request;
              HttpServletResponse httpResponse = (HttpServletResponse) response;
      
              chain.doFilter(request, response);
      
              if (httpResponse.getStatus() == HttpServletResponse.SC_OK) {
                  logger.info("Request to {} was successful", httpRequest.getRequestURI());
              }
          }
      
          @Override
          public void destroy() { // Cleanup logic if needed }
      }

Добавляем этот фильтр в SecurityFilterChain так, чтобы он сработал после [UsernamePasswordAuthenticationFilter](https://docs.spring.io/spring-security/site/docs/4.2.6.RELEASE/apidocs/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html) (применяем метод [*.addFilterAfter()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterAfter(jakarta.servlet.Filter,java.lang.Class))):

      import org.springframework.context.annotation.Bean;
      import org.springframework.context.annotation.Configuration;
      import org.springframework.security.config.annotation.web.builders.HttpSecurity;
      import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
      import org.springframework.security.web.SecurityFilterChain;
      import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
      
      @Configuration
      @EnableWebSecurity
      public class SecurityConfig {
      
          @Bean
          public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
              http.addFilterAfter(new LoggingFilter(), UsernamePasswordAuthenticationFilter.class)
                  .authorizeRequests()
                  .anyRequest().authenticated()
                  .and()
                  .httpBasic();
              
              return http.build();
          }
      }

Т.е. те же два выше описанных шага: создание фильтра с требуемой нам логикой обработки и внедрение его в цепочку Security 
FilterChain с требуемым методом и аргументами. 
________________________________________________________________________________________________________________________
### Замена существующего фильтра custom фильтром.

В некоторых случаях нам может понадобиться заменить существующий фильтр на наш собственный. Например, нам может 
понадобиться заменить BasicAuthenticationFilter предоставляемый по умолчанию на пользовательский механизм аутентификации
и тогда мы можем написать некий фильтр (ниже приведен шаблон, на этот раз мы наследуем от BasicAuthenticationFilter):

      import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
      import jakarta.servlet.Filter;
      import jakarta.servlet.FilterChain;
      import jakarta.servlet.FilterConfig;
      import jakarta.servlet.ServletException;
      import jakarta.servlet.ServletRequest;
      import jakarta.servlet.ServletResponse;
      import jakarta.servlet.http.HttpServletRequest;
      import jakarta.servlet.http.HttpServletResponse;
      
      import java.io.IOException;
      
      public class CustomAuthenticationFilter extends BasicAuthenticationFilter {

              // Custom logic for authentication...

      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
              HttpServletRequest httpRequest = (HttpServletRequest) request;
              HttpServletResponse httpResponse = (HttpServletResponse) response;
      
              //... Custom authentication logic here ...
      
              chain.doFilter(request, response);
          }
      }

Теперь надо заменить BasicAuthenticationFilter на CustomAuthenticationFilter (применяем метод [*.addFilterAt()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterAt(jakarta.servlet.Filter,java.lang.Class))):

      import org.springframework.context.annotation.Bean;
      import org.springframework.context.annotation.Configuration;
      import org.springframework.security.config.annotation.web.builders.HttpSecurity;
      import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
      import org.springframework.security.web.SecurityFilterChain;
      
      @Configuration
      @EnableWebSecurity
      public class SecurityConfig {
      
          @Bean
          public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
              http.addFilterAt(new CustomAuthenticationFilter(), BasicAuthenticationFilter.class)
                  .authorizeRequests()
                  .anyRequest().authenticated()
                  .and()
                  .httpBasic();
              
              return http.build();
          }
      }

Однако тут есть очень тонкий момент. Дело в том, что как таковой замены не происходит, дефолтный фильтр предоставляемый
Spring Security продолжает существовать, мы просто его подменяем, а если еще более точно - место расположения (ORDER) 
нашего custom фильтра совпадает с местом расположения предлагаемого по умолчанию Spring-ом. 

Тонкость данной ситуации в том, что регистрация нескольких фильтров в одном месте (с одним "порядковым номером") означает, 
что их порядок не является детерминированным (явно определенным). Более конкретно - регистрация нескольких фильтров в 
одном месте не переопределяет существующие фильтры. Чтобы не создавать путаницы, мы просто не должны регистрировать 
подменяемый дефолтный фильтр, который не хотим использовать (или пытаемся заменить).
________________________________________________________________________________________________________________________
И так, внедряя и настраивая пользовательские фильтры, мы можем расширить Spring Security для реализации конкретных 
требований безопасности нашего приложения. Если нам нужно добавить дополнительную проверку, ведение журнала или совершенно 
новые механизмы аутентификации, пользовательские фильтры предоставляют гибкий способ повышения безопасности веб-приложений.
________________________________________________________________________________________________________________________
### Часть 2. - Реализация самописных фильтров (filters) в Spring Security (практика).

Практика будет облегченной - покажем только технику внедрения фильтров в цепочку безопасности - [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/config/AppSecurityConfig.java).
Ни один из фильтров не будет как то менять или воздействовать на логику безопасности приложения - только логировать 
происходящее.

Создадим четыре фильтра:
- [MyFirstRememberMeBeforeFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/filters/MyFirstRememberMeBeforeFilter.java) - который будет внедрен перед UsernamePasswordAuthenticationFilter, основная логика 
его проста - логировать и выводить в консоль состояние cookie "remember-me", т.е. обрабатывать две ситуации: есть ли ключ 
"remember-me" в массиве cookie или нет (аутентифицировался ли пользователь в системе ранее и не выходил ли из нее после 
через logout). Для данного фильтра в FilterChain мы применяем метод - *.addFilterBefore().
- [MyAuthoritiesLoggingAfterFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/filters/MyAuthoritiesLoggingAfterFilter.java) - который будет внедрен после UsernamePasswordAuthenticationFilter фильтра и выведет 
в лог (консоль) данные уже аутентифицированного пользователя. Для данного фильтра в FilterChain мы применяем метод - *.addFilterAfter().
- [MyAuthoritiesLoggingAtFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/filters/MyAuthoritiesLoggingAtFilter.java) - данный фильтр просто логирует и выводит в консоль некий процесс. Для данного фильтра 
в FilterChain мы применяем метод - *.addFilterAt(). Мы не внесли ни какой дополнительной логики с ним, не исключили 
UsernamePasswordAuthenticationFilter из Security цепочки, по этому мы сможем наблюдать его появление перед основным фильтром.
- [MySecondRequestValidationBeforeFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/filters/MySecondRequestValidationBeforeFilter.java) - так же внедряется перед UsernamePasswordAuthenticationFilter, но реализует чуть
более сложную логику, чем [MyRequestValidationBeforeFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/filters/MySecondRequestValidationBeforeFilter.java). Мы обращаемся к remember-me cookie в request-е при его наличии 
и извлекаем userName из него см. комментарии к коду.

Для наглядности всего процесса мы сделали следующее:
- Шаг 1. - Добавили параметр в аннотацию [@EnableWebSecurity(debug = true)](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/config/AppSecurityConfig.java#L29), что позволяет фиксировать происходящее.
- Шаг 2. - Добавили в настройки отображения логгера следующий параметр в [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/resources/application.yml) (или строку application.properties): 

        logging.level.org.springframework.security.web.FilterChainProxy: DEBUG

После запуска приложения мы можем в консоли увидеть следующие "интимные" момента работы нашего приложения:
- Пользователь впервые вызывает страницу аутентификации (cookies пусты):

            ************************************************************
            Request received for GET '/webui/login':
            
            org.apache.catalina.connector.RequestFacade@2c8cd104
            
            servletPath:/webui/login
            pathInfo:null
            headers:
            host: localhost:8080
            connection: keep-alive
            pragma: no-cache
            cache-control: no-cache
            sec-ch-ua: "Not(A:Brand";v="99", "Google Chrome";v="133", "Chromium";v="133"
            sec-ch-ua-mobile: ?0
            sec-ch-ua-platform: "Windows"
            upgrade-insecure-requests: 1
            user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36
            accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
            sec-fetch-site: none
            sec-fetch-mode: navigate
            sec-fetch-user: ?1
            sec-fetch-dest: document
            accept-encoding: gzip, deflate, br, zstd
            accept-language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7
            
            ************************************************************            

            Security filter chain: [
                    DisableEncodeUrlFilter
                    WebAsyncManagerIntegrationFilter
                    SecurityContextHolderFilter
                    HeaderWriterFilter
                    CorsFilter
                    CsrfFilter
                    LogoutFilter
                    MySecondRequestValidationBeforeFilter
                    MyFirstRememberMeBeforeFilter
                    MyAuthoritiesLoggingAtFilter
                    UsernamePasswordAuthenticationFilter
                    MyAuthoritiesLoggingAfterFilter
                    RequestCacheAwareFilter
                    SecurityContextHolderAwareRequestFilter
                    RememberMeAuthenticationFilter
                    AnonymousAuthenticationFilter
                    ExceptionTranslationFilter
                    AuthorizationFilter
            ]
            ************************************************************

Содержимое запроса и структура цепочки фильтров - наши фильтры плотно обняли UsernamePasswordAuthenticationFilter, как мы
и задумывали см. [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/config/AppSecurityConfig.java), так же мы видим то, что возвращают в лог самописные фильтры при первом обращении к 
странице логина:

            2025-03-05T22:39:47.958+05:00 DEBUG 14476 --- [nio-8080-exec-2] o.s.security.web.FilterChainProxy : Securing GET /webui/login

            2025-03-05T22:39:47.970+05:00  INFO 14476 --- [nio-8080-exec-2] .f.MySecondRequestValidationBeforeFilter :  
            *** 2 - Log MySecondRequestValidationBeforeFilter method ***
            *** User try to authentication! Have no Remember-Me cookies! ***

            2025-03-05T22:39:47.971+05:00  INFO 14476 --- [nio-8080-exec-2] m.o.f.MyFirstRememberMeBeforeFilter :  
            *** 1 - Log MyFirstRememberMeBeforeFilter method ***
            *** User try to authentication! Have no Remember-Me cookies! ***

            2025-03-05T22:39:47.971+05:00  INFO 14476 --- [nio-8080-exec-2] m.o.f.MyAuthoritiesLoggingAtFilter :  
            *** 3 - Log MyAuthoritiesLoggingAtFilter method ***
            *** Method is in progress ***

Наши фильтры отработали, и поскольку cookie у нас чистые на старте (можно их почистить руками), то никаких данных о userName 
мы не видим. Теперь нужно залогиниться и посмотреть что будет (после удачной аутентификации нас автоматически перебросит на 
страницу [hello.html](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/resources/templates/hello.html)):

            Request received for GET '/webui/hello':
            
            org.apache.catalina.connector.RequestFacade@442786b2
            
            servletPath:/webui/hello
            pathInfo:null
            headers:
            host: localhost:8080
            connection: keep-alive
            pragma: no-cache
            cache-control: no-cache
            upgrade-insecure-requests: 1
            user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36
            accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
            sec-fetch-site: same-origin
            sec-fetch-mode: navigate
            sec-fetch-user: ?1
            sec-fetch-dest: document
            sec-ch-ua: "Not(A:Brand";v="99", "Google Chrome";v="133", "Chromium";v="133"
            sec-ch-ua-mobile: ?0
            sec-ch-ua-platform: "Windows"
            referer: http://localhost:8080/webui/login
            accept-encoding: gzip, deflate, br, zstd
            accept-language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7
            cookie: JSESSIONID=A6AB743FCFD8F028C7D3A94888AA6B25; 
                    remember-me=bkZUZFVySUdQQWtFJTJCMThjU2thdG9BJTNEJTNEOjFTNnZzWDl1YkhBWE1yWFFvdDF6ZVElM0QlM0Q

Видим, что появились cookie (JSESSIONID, remember-me). И в логах отработали наши фильтры, видим userName = admin@test.com:

            2025-03-05T22:47:58.091+05:00 DEBUG 14476 --- [nio-8080-exec-6] o.s.security.web.FilterChainProxy : Securing GET /webui/hello
            2025-03-05T22:47:58.094+05:00 DEBUG 14476 --- [nio-8080-exec-6] o.s.jdbc.core.JdbcTemplate : Executing prepared SQL query
            2025-03-05T22:47:58.094+05:00 DEBUG 14476 --- [nio-8080-exec-6] o.s.jdbc.core.JdbcTemplate : Executing prepared SQL statement [select username,series,token,last_used from persistent_logins where series = ?]
            2025-03-05T22:47:58.094+05:00 TRACE 14476 --- [nio-8080-exec-6] o.s.jdbc.core.StatementCreatorUtils : Setting SQL statement parameter value: column index 1, parameter value [nFTdUrIGPAkE+18cSkatoA==], value class [java.lang.String], SQL type unknown
            2025-03-05T22:47:58.099+05:00  INFO 14476 --- [nio-8080-exec-6] .f.MySecondRequestValidationBeforeFilter :  
            *** 2 - Log MySecondRequestValidationBeforeFilter method ***
            *** User admin@test.com is already authenticated by Remember-Me Token! ***
            2025-03-05T22:47:58.100+05:00  INFO 14476 --- [nio-8080-exec-6] m.o.f.MyFirstRememberMeBeforeFilter :  
            *** 1 - Log MyFirstRememberMeBeforeFilter method ***
            *** User is already authenticated by Remember-Me Token! ***
            2025-03-05T22:47:58.101+05:00  INFO 14476 --- [nio-8080-exec-6] m.o.f.MyFirstRememberMeBeforeFilter :  
            *** 1 - Log MyFirstRememberMeBeforeFilter method ***
            *** User admin@test.com is already authenticated by Remember-Me Token! ***
            2025-03-05T22:47:58.101+05:00  INFO 14476 --- [nio-8080-exec-6] m.o.f.MyAuthoritiesLoggingAtFilter :  
            *** 3 - Log MyAuthoritiesLoggingAtFilter method ***
            *** Method is in progress ***
            2025-03-05T22:47:58.102+05:00  INFO 14476 --- [nio-8080-exec-6] m.o.f.MyAuthoritiesLoggingAfterFilter :  
            *** 4 - Log MyAuthoritiesLoggingAfterFilter method ***
            *** User admin@test.com is successfully authenticated and has the authorities: [ROLE_ADMIN, MORE BAD ACTION, READ] ***
            2025-03-05T22:47:58.103+05:00 DEBUG 14476 --- [nio-8080-exec-6] o.s.security.web.FilterChainProxy : Secured GET /webui/hello

Отлично, теперь мы хотим перезапустить приложение, при этом снова руками почистить только JSESSIONID, а remember-me 
оставить. И снова обратиться к странице аутентификации или любой другой. Теоретически, при наличии только remember-me 
cookie в запросе, наш сервис (приложение) автоматически создаст сессию и по данным cookie автоматически аутентифицирует 
обратившегося, без обращения к форме аутентификации. Естественно при условии, что Remember-Me токен не просрочен. 

Делаем и смотрим что получилось (сразу обратимся к /webui/hello эндпоинту):

            2025-03-05T23:11:21.498+05:00 DEBUG 11224 --- [nio-8080-exec-2] o.s.security.web.FilterChainProxy : Securing GET /webui/hello
            2025-03-05T23:11:21.512+05:00 DEBUG 11224 --- [nio-8080-exec-2] o.s.jdbc.core.JdbcTemplate : Executing prepared SQL query
            2025-03-05T23:11:21.513+05:00 DEBUG 11224 --- [nio-8080-exec-2] o.s.jdbc.core.JdbcTemplate : Executing prepared SQL statement [select username,series,token,last_used from persistent_logins where series = ?]
            2025-03-05T23:11:21.522+05:00 TRACE 11224 --- [nio-8080-exec-2] o.s.jdbc.core.StatementCreatorUtils : Setting SQL statement parameter value: column index 1, parameter value [nFTdUrIGPAkE+18cSkatoA==], value class [java.lang.String], SQL type unknown
            2025-03-05T23:11:21.529+05:00  INFO 11224 --- [nio-8080-exec-2] .f.MySecondRequestValidationBeforeFilter :  
            *** 2 - Log MySecondRequestValidationBeforeFilter method ***
            *** User admin@test.com is already authenticated by Remember-Me Token! ***
            2025-03-05T23:11:21.530+05:00  INFO 11224 --- [nio-8080-exec-2] m.o.f.MyFirstRememberMeBeforeFilter :  
            *** 1 - Log MyFirstRememberMeBeforeFilter method ***
            *** User is already authenticated by Remember-Me Token! ***
            2025-03-05T23:11:21.530+05:00  INFO 11224 --- [nio-8080-exec-2] m.o.f.MyAuthoritiesLoggingAtFilter :  
            *** 3 - Log MyAuthoritiesLoggingAtFilter method ***
            *** Method is in progress ***

Из логов видно, что remember-me cookies в наличии и даже из них извлечен userName, однако обращения к БД еще не было и
режим авто-аутентификации не отработал. И только когда дело дошло до встроенного RememberMeAuthenticationFilter пошел 
запрос и сработал наш перехватчик фиксирующий процесс аутентификации - [AuthenticationEventListener.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_8_1/src/main/java/me/oldboy/config/auth_event_listener/AuthenticationEventListener.java): 

            Hibernate:
                select
                    c1_0.id,
                    c1_0.details_id,
                    c1_0.email,
                    c1_0.pass,
                    c1_0.role
                from
                    clients c1_0
                where
                    c1_0.email=?
            Hibernate:
                select
                    d1_0.id,
                    d1_0.client_age,
                    c1_0.id,
                    c1_0.email,
                    c1_0.pass,
                    c1_0.role,
                    loa1_0.client_id,
                    loa1_1.id,
                    loa1_1.authority_name,
                    d1_0.client_name,
                    d1_0.client_surname
                from
                  client_details d1_0
                left join
                    clients c1_0 on d1_0.id=c1_0.details_id
                left join
                    clients_authorities loa1_0 on c1_0.id=loa1_0.client_id
                left join
                    authorities loa1_1 on loa1_1.id=loa1_0.authority_id
                where
                    d1_0.id=?
            Login attempt with username: admin@test.com [ROLE_ADMIN, MORE BAD ACTION, READ]  me.oldboy.config.securiry_details.SecurityClientDetails@66987584 		
                                Success: true

И так, тут вкратце рассмотрен несложный пример работы самописных фильтров и их внедрение в цепочку безопасности.
________________________________________________________________________________________________________________________
Важные моменты. Необходимо разобраться с тем, как работают уже готовые фильтры Spring идущие сразу "из коробки". 

При решении задачи с извлечением данных из request "remember-me" cookie мы столкнулись с классом [PersistentTokenBasedRememberMeServices](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/authentication/rememberme/PersistentTokenBasedRememberMeServices.html) 
и его "родителем" [AbstractRememberMeServices](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/web/authentication/rememberme/AbstractRememberMeServices.html), 
которые имеют в своем арсенале интересовавшие нас методы и логику. И работающие четко с точки зрения безопасности, НО, 
весьма экстравагантно если применять их без понимания и должной осторожности. Особенно метод [*.autoLogin()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/web/authentication/rememberme/AbstractRememberMeServices.html#autoLogin(jakarta.servlet.http.HttpServletRequest,jakarta.servlet.http.HttpServletResponse)).

Поскольку наша задача была просто извлечь userName, то сразу приходит на ум вариант с получением откуда-нибудь [Authentication](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/core/Authentication.html).
А уже из него легко получить требуемое. Но мы помним, что до определенного момента у нас нет доступа к контексту безопасности, 
а если бы и был, он не содержал бы ничего до момента сработки авто-аутентификации при наличии нужных cookies или простой 
аутентификации через форму.

Нужен обходной путь! Сказано-сделано, используем класс [AbstractRememberMeServices](https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/authentication/rememberme/AbstractRememberMeServices.java) и его [*.autoLogin()](https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/authentication/rememberme/AbstractRememberMeServices.java#L126) 
метод, он возвращает требуемое. Фактически, нам нужно его пробросить в наш "...Before..." фильтр, да так, чтобы он был 
связан с текущим контекстом и просто применить выше указанный метод - он де извлечет все что надо. После реализации такой 
вроде бы не хитрой логики мы получили весьма интересный эффект см. внимательно метод который обрабатывает процесс авто-аутентификации 
[*.processAutoLoginCookie()](https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/authentication/rememberme/PersistentTokenBasedRememberMeServices.java#L95), 
класса [PersistentTokenBasedRememberMeServices.java](https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/authentication/rememberme/PersistentTokenBasedRememberMeServices.java).
Логика метода такова, что при определенных условиях, а именно при тех, что мы создали неумышленно (дублирование запуска 
авто-аутентификации в одной цепочке безопасности) - будет бросаться исключение:

        throw new CookieTheftException(this.messages.getMessage("PersistentTokenBasedRememberMeServices.cookieStolen",
                                                                "Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack."))

И естественно автоматически чиститься БД. Для неокрепшего ума это выглядит феерично!
________________________________________________________________________________________________________________________
### Key Class and Methods:

* [Class HttpSecurity](https://docs.spring.io/spring-security/site/docs/5.0.0.M5/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(javax.servlet.Filter,%20java.lang.Class))
* [Interface HttpSecurityBuilder](https://docs.spring.io/spring-security/site/docs/4.1.0.RC2/apidocs/org/springframework/security/config/annotation/web/HttpSecurityBuilder.html)

### Reference Documentation:

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