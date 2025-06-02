### Simple non Boot Spring App with Security (Part 7_2) - Немного об отличиях Role от Authority.

- Spring Core 6.2.2
- Spring Security 6.4.2
- Java 17
- Gradle
________________________________________________________________________________________________________________________

И снова вернемся к работе с non Boot Spring приложением. Тут мы сохраним основную структуру предыдущей части и как всегда
в таких случаях обратимся к TomCat для запуска нашего сервиса. Основные отличия от предыдущего варианта сервиса, это 
естественно раздел конфигурации, а так же расположение и настройка отображения. И естественно, поскольку мы решили выяснить 
отличия Roles от Authorities внесем изменения в структуру нашей базы данных.

Добавление наших вариантов "допусков" - Authorities в приложение:
- Шаг 1. - Добавим сущность [Auth](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/models/Auth.java), и создадим таблицу ["authorities"](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/DOC/SQL/scripts.sql#L55) в БД (пусть у нас будет 5-ть вариантов допусков).
- Шаг 2. - Создадим в БД таблицу ["clients_authorities"](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/DOC/SQL/scripts.sql#L66), связывающую наших [Client](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/models/Client.java) с их вариантами "допусков" - [Auth](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/models/Auth.java).
- Шаг 3. - Пропишем каждой сущности их связь, в данной ситуации пусть будет ManyToMany, повторим материал, хотя вариант 
OneToMany тоже подойдет. И так, прописали связи, задали вариант загрузки допусков вызванному клиенту как Fetch.EAGER
- Шаг 4. - Теперь необходимо прогрузить наши допуски в [SecurityClientDetails](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_details/SecurityClientDetails.jav) нашу реализацию UserDetails, вносим изменения 
в код метода [*.getAuthorities()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_details/SecurityClientDetails.java#L22).

      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(client.getRole().name()));
        authorities.addAll(client.getListOfAuth().stream()
                                                 .map(auth -> new SimpleGrantedAuthority(auth.getAuthName()))
                                                 .collect(Collectors.toList()));
        return authorities;
      }

- Шаг 5. - Немного изменим структуру файла [Role.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/models/Role.java) определяющего роли клиентов (Client), добавим к названиям префикс ROLE_ 
(внесем те же изменения в БД см. [scripts.sql](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/resources/db/changelog/db.changelog-5.0.sql));
- Шаг 6. - Корректируем цепочку фильтров в [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_config/AppSecurityConfig.java), применяем методы *.hasRole() и *.hasAuthority() к 
разным эндпоинтам. Так же для удобства тестирования мы разделили ранее "единую цепь защиты" на две.
- Шаг 7. - Для проверки работы внесенных изменений запускаем приложение в Debug режиме, можно увидеть список допусков и 
ролей аутентифицированного пользователя, прилетающий из [AuthenticationEventListener.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/auth_event_listener/AuthenticationEventListener.java) в консоль:

      Login attempt with username: admin@test.com [ROLE_ADMIN, MORE BAD ACTION, READ]  
                                   me.oldboy.config.security_details.SecurityClientDetails@ff2d420 		
                                   Success: true

Фактически "допуски" - Authorities, это более узкие права (правила), которые позволяют пользователю взаимодействовать с 
нашим сервисом (приложением), некие тонкие настройки, для конкретных действий. Роли или Roles можно описать, как "пакетные" 
или глобальные права пользователя на все приложение, которые могут в себе объединять более узкие варианты допусков в 
комплексе.
________________________________________________________________________________________________________________________
Поскольку мы перевели наше "Boot" приложение в "non Boot" формат нам необходимо настроить конфигурацию, взаимодействие 
с Thymeleaf и страницы отображения, а также их "симпатичную часть" - css файлы:
- Шаг 1. - Добавляем Thymeleaf зависимости в проект см. [build.gradle](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/build.gradle#L63).
- Шаг 2. - Добавляем в каталог WEB-INF папку [views](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_7_2/src/main/webapp/WEB-INF/views), тут будут находиться HTML страницы отображения.
- Шаг 3. - Добавляем в каталог webapp папку [static](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_7_2/src/main/webapp/static), тут будут находиться CSS файлы определяющий "красоту" наших frontend страниц.
- Шаг 4. - Создаем конфигурацию имплементирующую WebMvcConfigurer и определяющую работу отображений см. [ViewResolversConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/auth_view_init/ViewResolversConfig.java), в нем явно пришлось задать кодировку возвращаемых view и тип контента;
________________________________________________________________________________________________________________________
Тонкие моменты для первопроходцев:
- Для настройки статических ресурсов отображений нам нужно переопределить метод [*.addResourceHandlers()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/auth_view_init/ViewResolversConfig.java#L60) из интерфейса 
WebMvcConfigurer (для нашего случая):

      @Override
      public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
      }

- После аутентификации пользователь попадает на простую страницу, где отображается информация об аутентифицированном 
пользователе. При чем в двух форматах, через атрибут модели и через специальный тег Thymeleaf-а из Security контекста.
И если в Spring Boot приложении мы, как всегда, наблюдаем "магию" авто-конфигурирования, то тут нам понадобилось добавить
bean и прокинуть его в [SpringTemplateEngine](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/auth_view_init/ViewResolversConfig.java#L37):

      @Bean
      public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setAdditionalDialects(Set.of(getDialect()));
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
      }
  
      @Bean
      public SpringSecurityDialect getDialect(){
          return new SpringSecurityDialect();
      }

Данная ситуация и рекомендации описаны тут [Thymeleaf - Spring Security integration modules](https://github.com/thymeleaf/thymeleaf-extras-springsecurity).
- Так же в тестах, "вдруг", вместо ожидаемой UTF-8, мы стали получать непонятную кодировку страниц в ответ на запросы.
Для решения данной задачи, пришлось явно прописать настройки в [ThymeleafViewResolver](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/auth_view_init/ViewResolversConfig.java#L51):

      @Override
      public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setContentType("text/html; charset=UTF-8");
        registry.viewResolver(resolver);
      }
________________________________________________________________________________________________________________________
И так, на данном этапе у нас реализовано и переработано все из предшествующих частей:
- подключен [Spring Security](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_config/AppSecurityConfig.java) модуль;
- реализована from [base Authentication](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_config/AppSecurityConfig.java#L77) (PostgeSQL);
- реализована [Custom Login Form](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_config/AppSecurityConfig.java#L51);
- реализован пользовательский AuthenticationProvider, [UserDetail](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_details/SecurityClientDetails.java) и [UserDetailsService](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_details/ClientDetailsService.java);
- реализована [Remember-Me аутентификация](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/security_config/AppSecurityConfig.java#L48) ([ключи хранятся в БД](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/DOC/SQL/persistent_logins_token.sql));
- включена генерация CSRF-токена;
- включена [CORS защита](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/main/java/me/oldboy/config/auth_view_init/AuthBeanConfig.java#L25);
- применены как Roles, так и Authorities;
________________________________________________________________________________________________________________________
Код протестирован в нескольких режимах [unit](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_7_2/src/test/java/me/oldboy/unit) и [integration](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_7_2/src/test/java/me/oldboy/integration) - (глубина покрытия Class 90%(74/82), Method 85%(248/290), Line 89%(598/668)).
В интеграционных тестах применялся [Testcontainer](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/test/java/me/oldboy/config/test_data_source/TestContainerInit.java) с [PostgerSQL](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_7_2/src/main/resources/db/changelog) БД и миграционным фреймворком [Liquibase](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_2/src/test/java/me/oldboy/config/test_data_source/TestDataSourceConfig.java#L66).
________________________________________________________________________________________________________________________
### Reference Documentation:

* [Expression-Based Access Control](https://docs.spring.io/spring-security/site/docs/3.1.x/reference/el-access.html)
* [View Technologies](https://docs.spring.io/spring-framework/reference/web/webmvc-view.html)
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

### About Thymeleaf:

* [Thymeleaf Doc](https://www.thymeleaf.org/documentation.html)
* [GitHub: Thymeleaf - Spring Security integration modules](https://github.com/thymeleaf/thymeleaf-extras-springsecurity)

### Guides:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Spring Guides](https://spring.io/guides)
* [Spring Boot Security Auto-Configuration](https://www.baeldung.com/spring-boot-security-autoconfiguration)
* [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)
* [Spring Security: Upgrading the Deprecated WebSecurityConfigurerAdapter](https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter)

### Articles (question-answer):

* [Intro to Spring Security Expressions](https://www.baeldung.com/spring-security-expressions)
* [Difference Between hasRole() and hasAuthority() in Spring Security](https://www.geeksforgeeks.org/difference-between-hasrole-and-hasauthority-in-spring-security/)
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