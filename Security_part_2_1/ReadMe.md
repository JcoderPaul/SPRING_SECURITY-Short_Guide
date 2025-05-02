### Simple Spring Boot Security App (Step 2_1) - частичное ограничение доступа к ресурсам приложения (Light legacy).

- Spring Boot 2.6.15
- Spring Core 5.6.10
- Spring Security 5.6.10
- Java 8
- Maven

Следующий шаг. Мы добавили 6-ть страниц, часть из которых открыта всем, а часть доступна только после аутентификации.
Пароль и логин для доступа к закрытым страницам, через форму аутентификации (см. application.yml). Пара "ключ:значение" 
определяющие доступ заранее определены, название ключа взяты из документации, значение, естественно, задаем мы или 
Spring при запуске приложения предложит свой случайно сгенерированный. Используется относительно старая кодовая база.
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

