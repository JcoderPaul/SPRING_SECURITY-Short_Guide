### Simple non Boot Spring Security App (Part 4_2)

- Spring Core 6.2.2
- Spring Security 6.4.2
- Java 17
- Gradle

Данное приложение по структуре дублирует предыдущее, но оно non Boot и значит у него появятся дополнительные классы 
настройки (конфигурации) см. раздел - [config](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_4_2/src/main/java/me/oldboy/config). Так же изменился файл зависимостей - [build.gradle](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/build.gradle). И как всегда для его 
запуска нам нужен TomCat.

В данном demo-проекте мы используем БД и пару связанных двунаправленными отношениями one-to-one таблиц см. [scripts.sql ](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/scripts.sql)
и в данной реализации SQL скрипта таблица client_details ссылается на таблицу clients. Т.е. все с точностью до наоборот,
как и было обещано ранее. 

**Напоминашка:** Поскольку, мы используем одну и ту же БД от примера к примеру, и просто сносим и заново накатываем нужную 
нам структуру. Нужно не забыть снова накатить или запустить БД в Docker-e, что бы не удивляться, а, что это приложение 
падает или кидает ошибки, хотя вот только работало! 
________________________________________________________________________________________________________________________
Еще раз о валидации и глобальной обработке исключений:
- [Проверка (валидация) данных на уровне контроллера Spring.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/A_little_more_about_validation/ValidatingDataInControllerLayer.md)
- [Применение @ExceptionHandler для обработки исключений в Spring приложениях.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/A_little_more_about_validation/SpringExceptionHandler.md)
- [Примеры использования BindingResult в Spring.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/A_little_more_about_validation/BindingResultInSpringExample.md)
- [Применение BindingResult и @ExceptionHandler.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/A_little_more_about_validation/BindingResultExceptionHandler.md)

Немного о тестировании слоя контроллеров:
- [Тестирование @ExceptionHandler в Spring контроллерах.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/About_Validation_Tests/ExceptionHandlerTest.md)
- [Тестирование обработчика исключения в Spring приложениях.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/About_Validation_Tests/TestingControllerLayer.md)
- [Тестирование уровня контроллера применяющего @ExceptionHandler в Spring nonBoot приложениях.](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/DOC/About_Validation_Tests/TestingControllerNonBootSpring.md)
________________________________________________________________________________________________________________________
В данном приложении мы тоже проводили тесты - [test (покрытие Class 94%(64/68), Method 83%(182/218), Line 88%(406/458))](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_4_2/src/test)
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