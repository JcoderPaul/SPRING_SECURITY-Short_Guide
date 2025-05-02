### First simple Spring Boot Security App - настройки по умолчанию.

- Spring Boot 2.3.3 RELEASE
- Spring Security 5.3.4 RELEASE
- Spring Core 5.2.8 RELEASE
- Java 8
- Maven

Чтобы включить Spring Security в проекте, достаточно добавить:
- Maven зависимость:
  
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

- Gradle зависимость:

        implementation 'org.springframework.boot:spring-boot-starter-security'

Теперь при попытке ввести в браузере наше единственный на данный момент эндпоинт: http://localhost:8080/welcome - 
мы перенаправляемся на страницу логина http://localhost:8080/login. Если ввести в нее любые наугад взятые данные, 
в самой форме аутентификации будет брошено исключение - Bad credential.

Данные предоставленные Spring Security по умолчанию: Логин - user, пароль генерируется и выводится в консоли в виде 
набора чисел при запуске программы.

И так, мы добавили в проект зависимость Spring Security, что дальше:

- Шаг 1. - Spring Security создает пользователя с именем user и автоматически сгенерированным паролем, который можно посмотреть (забрать) в консоли.
- Шаг 2. - Автоматически Spring предоставляет страницу с формой для ввода имени и пароля, это Form-based аутентификация.
- Шаг 3. - Spring Security проверяет введенные данные - соответствуют предложенным по умолчанию - нас пропускают дальше на защищенную страницу, нет - бросается ошибка.
- Шаг 4. - До окончания аутентификации все эндпоинты (URL страниц приложения) оказываются недоступны, пока мы не «залогинимся» под предложенным пользователем.
- Шаг 5. - После аутентификации мы получаем доступ к защищенным (эндпоинтам) страницам сервиса (приложения), мы так же можем выйти из приложения (сервиса) - Spring-ом создается страница (эндпоинт) /logout, где можно «разлогиниться».

### In-Memory аутентификация.

С точки зрения получения параметров пользователя из запроса, продемонстрированная выше аутентификация является Form-Based - 
имя и пароль отправляются через форму и берутся на сервере из запроса как POST-параметры.

С точки зрения же хранения пользователей на стороне сервера, продемонстрированная выше аутентификация в Spring Security 
называется In-Memory authentication. Она означает, что пользователь хранится не в базе, не на LDAP-сервере и не где-либо 
еще, а в оперативной памяти приложения до тех пор, пока оно запущено. И чтобы отредактировать пользователя, придется 
заново запускать приложение. Разумеется, этот вариант не годится для среды Production, зато он прост и полезен для 
экспериментов во время разработки и изучения сути вопроса.

### Как задать своего пользователя в In-Memory аутентификации.

Итак, приложение при запуске генерирует и хранит имя и пароль пользователя в памяти, мы можем подсмотреть пароль в 
консоли - вариант первый и описан выше.

Но чтобы не искать пароль в консоли, можно воспользоваться файлом настроек [application.yml ](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/src/main/resources/application.yml)и прописать имя/пароль там.

В случае настроек доступа в файле application.properties или [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/src/main/resources/application.yml) приложение не генерирует открытый пароль в консоли,
мы перекрываем дефолтные настройки Spring Security.

Резюме: При запуске приложения с дефолными настройками безопасности запускается встроенный механизм Spring Security и 
демонстрируется страница авторизации. Если пароль и логин прописаны в [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/src/main/resources/application.yml), то дефолтные пароль и логин недоступны. 
По умолчанию закрыты все страницы, кроме стр. аутентификации. Структура ключей в *.yaml файле специфична для Spring Boot 
приложения. 

Фактически, для демонстрации работы достаточно прописать необходимые зависимости в [pom.xml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/pom.xml), создать один эндпоинт в 
[WelcomeController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/src/main/java/me/oldboy/controller/WelcomeController.java) и запускаемый файл [SpringSecurityBasicApp.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/src/main/java/me/oldboy/SpringSecurityBasicApp.java).

________________________________________________________________________________________________________________________
Для тестирования нашего пока что единственного, но защищенного контроллера нужно вспомнить материал из [Lesson 112 - Security-Testing](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_21#lesson-112---security-testing).
Необходимые зависимости у нас есть, прописываем требуемые аннотации и тестируем контроллер - [WelcomeControllerTest.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/src/test/java/me/oldboy/controller/WelcomeControllerTest.java)
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

### Old material (repeat):

* [Spring Boot lessons part 20 - Security Starter - PART 1](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_20)
________________________________________________________________________________________________________________________

