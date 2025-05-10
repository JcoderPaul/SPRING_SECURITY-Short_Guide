### Simple Spring Boot Security App (Part 5_2) - пользовательский AuthenticationProvider, UserDetailsService и UserDetails.

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle

На предыдущем шаге - Security_part_4_1 - мы реализовали наши кастомный UserDetailsService и UserDetails, где "объяснили" Spring Security как и 
откуда брать данные для аутентификации пользователей нашего сервиса (приложения). 

Далее мы реализовали наш собственный AuthenticationProvider - Security_part_5_1. 

При реализации собственного AuthenticationProvider требуется переопределить метод *.authenticate() в котором нужно:
- самостоятельно сравнить полученный из БД пароль и логин с переданным из формы и выбросить исключение если это необходимо;
- вернуть UsernamePasswordAuthenticationToken, который расширяет AbstractAuthenticationToken, а тот в свою очередь реализует Authentication;

Получается следующее, метод *.authenticate() нашего CustomAuthProvider-а принимает на вход параметр Authentication, который
уже содержит имя и пароль, взятые из запроса/из формы. Если аутентификация прошла успешно, мы извлекаем из БД сведения об 
аутентифицированном пользователе, а значит у нас есть все, чтобы создать, заполнить и вернуть приложению новый UsernamePasswordAuthenticationToken:

    public UsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) 

И так, полученными из БД сведениями заполняем поля principal и credentials объекта Authentication, формируем его и 
возвращаем из метода *.authenticate(). В новом UsernamePasswordAuthenticationToken реальный пользователь из БД лежит 
в Principal в виде UserDetails, а вернее мы помещаем все необходимое в него сами.

См. [описание работы DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html), очень похоже.

Не сложно заметить, что в структуре нашего CustomAuthProvider-a в неявном виде присутствует код работавший ранее в наших
реализациях UserDetailsService и UserDetails. Для наглядности выделим их.

Теперь у нас есть наши собственные кастомные:
- SecurityClientDetails реализация [UserDetails](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html) - Предоставляет основную информацию о пользователе. Реализации не используются 
Spring Security напрямую в целях безопасности. Они просто хранят информацию о пользователе, которая позже инкапсулируется 
в объекты аутентификации - [Authentication](https://docs.spring.io/spring-security/reference/features/authentication/index.html). Это позволяет хранить не связанную с безопасностью информацию о пользователе 
(такую как адреса электронной почты, номера телефонов и т. д.) в удобном месте.
- ClientDetailsService реализация [UserDetailsService](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/core/userdetails/UserDetailsService.html) - Основной интерфейс, который загружает пользовательские данные. Он 
используется во всей структуре как пользовательский DAO и является стратегией, используемой DaoAuthenticationProvider-ом. 
Интерфейс требует реализации одного метода только для чтения, что упрощает поддержку новых стратегий доступа к данным.
- CustomAuthProvider реализация [AuthenticationProvider](https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/authentication/AuthenticationProvider.html);
________________________________________________________________________________________________________________________
### Reference Documentation:

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

* [Spring Security – UserDetailsService and UserDetails with Example](https://www.geeksforgeeks.org/spring-security-userdetailsservice-and-userdetails-with-example/)
* [@Valid Annotation on Child Objects](https://www.baeldung.com/java-valid-annotation-child-objects)
* [Java Bean Validation Basics](https://www.baeldung.com/java-validation)
* [Javax validation on nested objects - not working](https://stackoverflow.com/questions/53999226/javax-validation-on-nested-objects-not-working)
* [Проверка данных — Java & Spring Validation](https://habr.com/ru/articles/424819/)
________________________________________________________________________________________________________________________