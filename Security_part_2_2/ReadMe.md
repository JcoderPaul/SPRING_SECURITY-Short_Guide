### Simple Spring Boot Security App (Part 2_2) - частичное ограничение доступа к ресурсам приложения (свежая кодовая база).

- Spring Boot 3.3.8
- Spring Core 6.1.16
- Spring Security 6.3.6
- Java 17
- Gradle

Следующий шаг - используем более современную кодовую базу. Условия те же, что и на предыдущем шаге, добавили 6-ть страниц, 
часть из которых открыта всем, а часть доступна только после аутентификации. Пароль и логин для доступа к закрытым страницам, 
через форму аутентификации (см. application.yml). 

Основной интерес представляет файл конфигурации (отсюда и далее):
- SecurityConfig.java - файл настройки системы защиты Spring-a;
- build.gradle - файл зависимостей;
________________________________________________________________________________________________________________________
### Reference Documentation:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Reference Guide (history)](https://docs.spring.io/spring-boot/docs/)
* [Spring Security](https://spring.io/projects/spring-security)
* [Spring Security Examples](https://spring.io/projects/spring-security#samples)

### Guides:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Spring Guides](https://spring.io/guides)
________________________________________________________________________________________________________________________