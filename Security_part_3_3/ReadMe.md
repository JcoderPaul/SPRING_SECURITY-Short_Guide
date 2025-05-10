### Simple (non Boot) Spring App with Security module (Part 3_3)  - получение данным из БД и настройка авторизации краткий пример.

- Spring Core 6.2.2
- Spring Security 6.4.2
- Java 17
- Gradle

В данном примере мы продолжаем исследовать работу Spring Security приложения, но, на этот раз в non Boot варианте. 
Т.е. для запуска оного нам понадобится TomCat или любой другой контейнер сервлетов. В прошлом разделе мы развернули 
и настроили БД согласно определенным принципам. Таблицы в ней имеют поля с четко заданными названиями и связями. Это 
позволяет до определенного момента избежать использования (реализации) интерфейса [UserDetails](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html) 
([см. док](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details.html)).

В данном случае мы снова используем сборщик Gradle. И поскольку это non Boot приложение, то часть магии автоконфигурирования 
исчезает и появляются дополнительные конфигурационные классы без которых система безопасности Spring-a работать не будет.

- [AbstractSecurityWebApplicationInitializer](https://docs.spring.io/spring-security/site/docs/4.2.4.RELEASE/apidocs/org/springframework/security/web/context/AbstractSecurityWebApplicationInitializer.html) - гарантирует, что springSecurityFilterChain будет зарегистрирован в контексте ([см. оф.док и примеры кода](https://docs.spring.io/spring-security/site/docs/4.2.x/reference/html/jc.html#abstractsecuritywebapplicationinitializer)).
- [AbstractAnnotationConfigDispatcherServletInitializer](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/support/AbstractAnnotationConfigDispatcherServletInitializer.html) - упрощает регистрацию DispatcherServlet-a путем простого переопределения методов для указания сопоставления сервлета и расположения конфигурации DispatcherServlet ([см. оф.док](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html#mvc-container-config)). 

Настроить ServletContext в Spring можно двумя путями, создав класс - конфигурацию и:
- реализовать интерфейс WebApplicationInitializer и [переопределить метод onStartup](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html) (как мы делали это ранее, [см. пример](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/config/WebContextInitializer.java));
- унаследовать класс [AbstractAnnotationConfigDispatcherServletInitializer](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/main/java/me/oldboy/config/AppWebInitializer.java) (см. тек. реализацию класса и [оф.док Spring 6.2.2](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/container-config.html));

Теоретически реализовав интерфейс WebApplicationInitializer мы можем полностью сконфигурировать контекст, но вручную. 
Расширяя же AbstractAnnotationConfigDispatcherServletInitializer мы получаем почти готовый сервлет-контекст, и нам 
остаётся только реализовать методы getRootConfigClasses() и getServletConfigClasses(), как это указано в документации.

Первый способ можно использовать, когда нужен полный контроль над процессом инициализации сервлет-контекста, второй - когда 
достаточно стандартного процесса инициализации. Это применимо и к другим абстрактным реализациям WebApplicationInitializer.

Основной интерес представляют файлы конфигурации:
- [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/main/java/me/oldboy/config/security/AppSecurityConfig.java) - файл настройки системы безопасности Spring-a;
- [AppSecurityInitializer.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/main/java/me/oldboy/config/security/AppSecurityInitializer.java) - файл регистрирующий нашу цепочку фильтров FilterChain в контейнере сервлетов;
- [AppWebInitializer.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/main/java/me/oldboy/config/web_inint/AppWebInitializer.java) - файл инициализирующий сервлет-контекст;
- [DataSourceConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/main/java/me/oldboy/config/data_source/DataSourceConfig.java) - файл конфигурации источника данных (в Spring Boot приложении все происходило автомагически, 
данные для настройки связи с БД и т.д., брались из [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/main/resources/application.yml) или application.properties, а необходимые bean-ы 
загружались в контекст приложения без нашего участия);
- [build.gradle](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/edit/master/Security_part_3_3/build.gradle) - файл зависимостей;

В данном разделе для тестирования мы использовали InMemory H2 Base. Для этого в конфигурационном файле gradle добавляем 
нужную зависимость:

        testImplementation "com.h2database:h2:${versions.h2}"

Для тестов нам понадобится тестовое окружение (по крайней мере [для одного теста точно](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/test/java/me/oldboy/integration/controller/RoleControllerTest.java)) и [файл свойств](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/test/resources/application-test.yml), а также [скрипт тестовой БД](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_3/src/test/resources/sql_scripts/data.sql):
- тестовые ресурсы - [resources](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_3/src/test/resources);
- файлы формирующие тестовый контекст - [test_context](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_3/src/test/java/me/oldboy/config/test_context);
________________________________________________________________________________________________________________________
### Reference Documentation:

* [Web on Servlet Stack](https://docs.spring.io/spring-framework/reference/web.html)
* [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
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
________________________________________________________________________________________________________________________