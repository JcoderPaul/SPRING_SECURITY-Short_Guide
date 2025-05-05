### Simple Spring Boot Security App (Part 2_2) - частичное ограничение доступа к ресурсам приложения (свежая кодовая база).

- Spring Boot 3.3.8
- Spring Core 6.1.16
- Spring Security 6.3.6
- Java 17
- Gradle

Следующий шаг - используем более современную кодовую базу. Условия те же, что и на предыдущем шаге, добавили 6-ть страниц, 
часть из которых открыта всем, а часть доступна только после аутентификации. Пароль и логин для доступа к закрытым страницам, 
через форму аутентификации (см. [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_2/src/main/resources/application.yml)). 

Основной интерес представляет файл конфигурации (отсюда и далее):
- [SecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_2/src/main/java/me/oldboy/config/SecurityConfig.java) - файл настройки системы защиты Spring-a;
- [build.gradle](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_2/build.gradle) - файл зависимостей;
________________________________________________________________________________________________________________________
- тестирование контроллеров примеры - [controller](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_2_2/src/test/java/me/oldboy/controller);
- аннотации применяемые в тестовом разделе описаны в - [Немного теории (@SpringBootTest, @AutoConfigureMockMvc, @WebMvcTest, @WithMockUser)](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_2_1#%D0%BD%D0%B5%D0%BC%D0%BD%D0%BE%D0%B3%D0%BE-%D1%82%D0%B5%D0%BE%D1%80%D0%B8%D0%B8-springboottest-autoconfiguremockmvc-webmvctest-withmockuser);
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