### Simple Spring Boot Security App (Part 3_4)

- Spring Boot 3.3.8
- Spring Core 6.1.16
- Spring Security 6.3.6
- Java 17
- Gradle

В предыдущей части мы собрали и сконфигурировали non Boot Spring приложение. Теперь сделаем то же, но в Spring Boot формате.
БД у нас уже есть, таблицы настроены, теперь самое интересное. Если сравнивать папки config текущего и предыдущего проекта,
то видно, что тут остался единственный файл [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_4/src/main/java/me/oldboy/config/AppSecurityConfig.java). 

Так же, за счет того, что в Spring Boot интегрирован TomCat (что видно в логах, при запуске), мы можем стандартным образом 
настроить запускаемый файл и все. У нас это [SecurityDemoAppPart_3_4.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_4/src/main/java/me/oldboy/SecurityDemoAppPart_3_4.java) - запускаем и через браузер обращаемся к нашим 
конечным точкам описанным в классах контролерах. 

Как и раньше файл [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_4/src/main/resources/application.yml) задает предварительные настройки приложения, специальных классов для его чтения создавать 
не надо, Spring Boot читает его "из коробки". 

В итоге, у нас сравнительно меньше кода чем было в non Boot приложении и ненужно держать отдельный web-сервер или контейнер 
сервлетов, все идет "из коробки" - много магии и никакого мошенничества!

Основной интерес представляет файл конфигурации (особенно настройка цепочки фильтров):
- [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_4/src/main/java/me/oldboy/config/AppSecurityConfig.java) - файл настройки системы защиты Spring-a;
- [build.gradle](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_4/build.gradle) - файл зависимостей;

И так, в этом и предыдущих разделах (примерах) мы работали с некими стандартизированными настройками (именованием) полей 
таблиц и их структурой, но в реальности приходится конструировать структуру БД под требования заказчика или прикручивать
(модернизировать готовое) приложение к уже существующей БД в которой не будет нужных таблиц/полей, а процедуру аутентификации
делать нужно. 

Тут нам на помощь приходит интерфейс UserDetails. Его применение рассмотрим в следующем разделе.
________________________________________________________________________________________________________________________
Для понимания отличия настройки тестирования nonBoot и Boot приложений можно изучить:
- [тесты текущего (Boot приложения)](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_4/src/test) (покрытие: Классы: 100%(22/22), Методы: 90%(56/62), Строки: 93%(122/130)); 
- [тесты предыдущего (non Boot приложения)](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_3/src/test) (покрытие: Классы: 93%(28/30), Методы: 84%(55/64), Строки: 93%(160/172));
________________________________________________________________________________________________________________________
### Reference Documentation:

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