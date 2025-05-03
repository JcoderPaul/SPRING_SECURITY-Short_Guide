### Simple Spring Boot Security App (Part 3_1) - использование нескольких пользователей при доступе к приложению (in-memory аутентификация).

- Spring Boot 2.6.15 
- Spring Core 5.3.27
- Spring Security 5.6.10
- Java 8
- Maven

В данном примере мы в файл application.properties добавим еще одного пользователя, а файл LikeBase.java будет имитировать БД. 
В конфигурационном файле мы извлечем сведения обоих пользователей и поместим их "память", откуда они будут извлекаться по
мере обращения каждого из пользователей к приложению и сравниваться с их "допусками". Пароль и логин для доступа к 
закрытым страницам снова получаем через встроенную в Spring форму аутентификации. 

Любопытная ситуация возникла когда в файл application.properties был помещен пользователь user.name, оказывается, такой 
ключ соответствует системным настройкам Windows и если применить его, то вместо заданного в файле настроек мы получим 
данные системных настроек - имя пользователя компьютера см.:
- [Why @Value("${user}") always is my the username of the computer](https://stackoverflow.com/questions/48677023/why-valueuser-always-is-my-the-username-of-the-computer);
- [Externalized Configuration](https://docs.spring.io/spring-boot/docs/1.5.10.RELEASE/reference/htmlsingle/#boot-features-external-config);

Дабы выйти из такого положения придумываем новое название ключа - simple.user.name
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