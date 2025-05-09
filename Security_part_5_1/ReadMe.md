### Simple Spring Boot Security App (Part 5_1) - пользовательский AuthenticationProvider.

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle

На предыдущем шаге мы реализовали наши кастомный UserDetailsService и UserDetails, где "объяснили" Spring Security как и 
откуда брать данные для аутентификации пользователей нашего сервиса (приложения). У нас использовался DaoAuthenticationProvider
предоставляемый системой безопасности Spring-a по-умолчанию. Теперь мы хотим реализовать наш собственный AuthenticationProvider. 

При реализации собственного AuthenticationProvider требуется переопределить метод *.authenticate() в котором нужно:
- самостоятельно сравнить полученный из БД пароль и логин с переданным из формы и выбросить исключение если это необходимо;
- вернуть UsernamePasswordAuthenticationToken, который расширяет AbstractAuthenticationToken, а тот в свою очередь реализует Authentication;

Получается следующее, метод *.authenticate() нашего CustomAuthProvider-а принимает на вход параметр Authentication, который
уже содержит имя и пароль, взятые из запроса/из формы. Если аутентификация прошла успешно, мы извлекаем из БД сведения об 
аутентифицированном пользователе, а значит у нас есть все, чтобы создать, заполнить и вернуть приложению новый UsernamePasswordAuthenticationToken:

    public UsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) 

И так, полученными из БД сведениями заполняем поля principal и credentials объекта Authentication, формируем его и 
возвращаем из метода *.authenticate(). В новом UsernamePasswordAuthenticationToken реальный пользователь из БД лежит 
в Principal в виде UserDetails.

Не сложно заметить, что в структуре нашего CustomAuthProvider-a в неявном виде присутствует код работавший ранее в наших
реализациях UserDetailsService и UserDetails. Для наглядности выделим его и вынесем пример в отдельное приложение см. след.

Для наглядности процесса аутентификации реализуем слушатель (фильтр) AbstractAuthenticationEvent. Он позволяет выводить в 
консоль результат работы метода аутентификации.
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