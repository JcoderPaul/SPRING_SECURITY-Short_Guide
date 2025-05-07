### Simple Spring Boot Security App (Part 4_1) - пользовательская аутентификация в Spring Security (кастомный UserDetailsService и UserDetails).

- Spring Boot 3.3.8
- Spring Core 6.1.16
- Spring Security 6.3.6
- Java 17
- Gradle

В данном приложении мы решаем основную задачу и несколько не связанных друг с другом и с безопасностью задач:
- Аутентифицируем клиента (пользователя сервиса) доставая его данные из БД, но поля таблиц не специфичны.
- Применяем наследника UserDetailsService и UserDetails для стыковки инфраструктуры безопасности Spring-a и данных из базы.
- Применяем кодирование паролей Bcrypt;
- Применение Authentication, SecurityContextHolder и метода *.getPrincipal() для получения данных об аутентифицированном клиенте в контроллере приложения. 
- Используем межслойные классы DTO.
- Валидируем входящие DTO (cтандартным способом и самописным валидатором).
- Применяем самописные исключения и глобальный обработчик исключений.

И так, поехали, ранее мы использовали специфические названия таблиц и полей в БД, что позволяло с определенной простотой 
использовать извлеченные из базы сведения для аутентификации обращающегося к нашему сервису (приложению) клиента. Теперь
же мы собрали некую БД (см. scripts.sql скрипт), заполнили ее данными и хотим использовать эти данные для аутентификации 
клиентов. Хотя у нас нет таблицы users (и полей username/password), мы можем "объяснить" Spring Security что и откуда брать.
- Шаг 1. - Создаем класс SecurityClientDetails имплементируем [UserDetails](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html) и реализуем его методы, нам важны [*.getUsername()](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html#getUsername()) и [getPassword()](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html#getPassword()) ([см. оф.док.](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details.html)).
- Шаг 2. - Создаем класс ClientDetailsService наследуем от [UserDetailsService](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/core/userdetails/UserDetailsService.html) и реализуем единственный метод [*.loadUserByUsername()](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/core/userdetails/UserDetailsService.html#loadUserByUsername(java.lang.String)) ([см. оф.док.](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html)).
- Шаг 3. - Создаем, аннотируем и конфигурируем AppSecurityConfig (важная честь это bean getPasswordEncoder(), который мы будем применять для шифрования и сравнения паролей).

Фактически эти три шага закрывают вопрос безопасности на минимальном стандартном уровне. Что мы сделали далее (прочие доработки):

- Шаг 4. - Добавили DTO классы для межслойного общения (у нас же MVC структура). Именно эти классы будут прилетать и 
улетать из приложения со слоя контроллеров. И именно прилетающие DTO (содержащие слово Create) будут валидироваться на входе, 
а не наши ключевые (entity) сущности, которыми мы манипулируем на слое репозиториев.
- Шаг 5. - Создаем самописный валидатор для работы с ClientCreateDto в котором существует вложенный DetailsCreateDto. 
Для этого нам нужна аннотация - назовем ее @CheckDetails и создадим интерфейс - CheckDetails.java и сам валидатор - 
DetailsValidator.java. Теоретически, нам бы хватило аннотации @Valid над полем вложенного класса см. ClientCreateDto.java 
и классических аннотаций из пакета jakarta.validation.constraints, что мы и сделали. Но написать и использовать свой 
валидатор все же интереснее.
- Шаг 6. - Написали пару исключений и обработчик оных см. exception.

Описывать структуру проекта смысла нет она просто расширяется от шага к шагу, см. предыдущие.

Любопытные моменты это реализация связи наших ключевых классов и системы безопасности Spring см. папку securiry_details.
Так же привязка текущего потока к контексту безопасности и манипуляции с объектом аутентификации см. комментарии в 
ClientController.java к реализации метода *.getClientList().   

В данном demo-проекте мы используем БД и пару связанных двунаправленными отношениями one-to-one таблиц см. scripts.sql 
и в данной реализации SQL скрипта таблица clients ссылается на таблицу client_details. В следующем non Boot проекте,
который будет почти дублировать текущий, за исключением конфигурации и зависимостей, мы сделаем обратную ссылку.
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

### Articles (question-answer):

* [@Valid Annotation on Child Objects](https://www.baeldung.com/java-valid-annotation-child-objects)
* [Java Bean Validation Basics](https://www.baeldung.com/java-validation)
* [Javax validation on nested objects - not working](https://stackoverflow.com/questions/53999226/javax-validation-on-nested-objects-not-working)
* [Проверка данных — Java & Spring Validation](https://habr.com/ru/articles/424819/)
________________________________________________________________________________________________________________________