### Simple Spring Boot Security App (Part 3_2) - получение данных из БД и настройка авторизации краткий пример.

- Spring Boot 2.6.15
- Spring Core 5.3.27
- Spring Security 5.6.10
- Java 8
- Maven

Теперь получим данные аутентификации из БД, как обычно, используем Docker для развертывания PostgrSQL: 

        docker run --name secur_test -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -p 5437:5432 -d postgres:13

Разместим в базу немного данных:

        CREATE TABLE users (
            id BIGSERIAL PRIMARY KEY ,
            username varchar(65) unique,
            password varchar(100),
            enabled int
        ) ;
        
        CREATE TABLE authorities (
            id BIGSERIAL PRIMARY KEY ,
            username varchar(65),
            authority varchar(25),
            FOREIGN KEY (username) references users(username)
        ) ;
        
        INSERT INTO users (username, password, enabled)
        VALUES ('Paul', '{noop}12345', 1),
               ('Sasha', '{noop}54321', 1),
               ('Stasya', '{noop}98765', 1);
        
        INSERT INTO authorities (username, authority)
        VALUES ('Paul', 'ROLE_EMPLOYEE'),
               ('Sasha', 'ROLE_HR'),
               ('Stasya', 'ROLE_HR'),
               ('Stasya', 'ROLE_MANAGER');

Повторим:
- Аутентификация - проверка пользователя на то, является ли он тем, кем себя выдает. Приложение проверяет, что такому имени действительно соответствует такой пароль и отвечает ок, если проверка пройдена.
- Авторизация — это выдача прав (либо отказ в таковых). Происходит уже после того, как пользователь подтвердил свою идентичность. 

Допустим, пользователь прошел аутентификацию и хочет попасть на некий защищенный эндпоинт (URL). Приложение проверяет, 
какие стоят права у данного пользователя, и либо позволяет посещение (дает доступ к эндпоинту), либо нет.

Тут присутствует тонкий момент, для того чтобы защитные функции Spring-a заработали без видимых "гвоздей", при запуске 
приложения таблицы в БД и ее поля должны иметь заранее определенный, выше приведенный, вид. В случае если названия полей
и таблиц будут отличаться от "канонических" нам придется произвести дополнительные настройки и "сведение" данных, что 
будет рассмотрено позже.

#### Настройка авторизации.

Чтобы настроить авторизацию, надо точно так же, как мы делали при настройке аутентификации, переопределить метод 
[*.configure()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_2/src/main/java/me/oldboy/config/SecurityConfig.java#L43) в старых версиях Spring и метод *.filterChain() в новых, только теперь с другим аргументом HttpSecurity, см.
пример [SecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_2/src/main/java/me/oldboy/config/SecurityConfig.java)

Именно этот объект HttpSecurity и нужно настраивать. Создавать его как бин не надо, его создает Spring Security, а мы 
получаем к нему доступ из метода configure(HttpSecurity http). Несложно заметить, что по умолчанию Spring Security дает 
доступ к любому url любому аутентифицированному пользователю. Т.е. если хочешь попасть на url, перенаправляешься 
на форму ввода пароля, и сразу после этого получаешь доступ - это если при дефолтных настройках.

Переопределив метод configure(HttpSecurity http) или *.filterChain(), мы отменяем поведение по умолчанию. Теперь внутри 
переопределенного метода необходимо задать все доступы заново. Мы по очереди перечисляем возможные эндпоинты и задаем 
права доступа к ним (в нашем примере это роли).

При настройке доступа, обычно, мы перечисляем не сами url (поскольку их может быть слишком много), а шаблоны. 
Шаблоны url эндпоинтов задаются с помощью класса AntPathRequestMatcher. Перечислять шаблоны надо в порядке от самых 
узкоохватывающих до широкоохватывающих. Если в коде начать перечисление с всеобъемлющего шаблона /**, то перебор на 
нем остановится (так как любой url, в том числе /admin) соответствует шаблону /** , а значит всем будет разрешен доступ. 
Именно поэтому начинать нужно с узкоохватывающих шаблонов.

#### Настройка доступа (роли, разрешения).

Наконец, к главному. После шаблона в каждой строке указывается кому разрешен доступ: 
- всем пользователям (метод permitAll() разрешает доступ всем, в том числе неаутентифицированным пользователям);
- пользователям с определенной ролью — метод hasRole("ADMIN"), (либо ролями) hasAnyRoles("ADMIN", "HR").

В настройках аутентификации в мы задавали пользователям разрешение с префиксом ROLE. А в настройках авторизации доступ 
определяем через роль. Роль идет без префикса ROLE - таково соглашение. Можно было задать доступ с помощью разрешений, 
*.hasAuthority("ROLE_ADMIN") или *.hasAnyAuthority("ROLE_USER", "ROLE_ADMIN"). Метод *.authenticated() - разрешает 
доступ всем аутентифицированным пользователям.

Как только мы начали кастомизацию настроек безопасности страница ввода пароля генерироваться уже не будет, чтобы ее 
вернуть в отображение необходимо дописать: *.and().formLogin();

Если пользователь не аутентифицирован (в данном случае так будет, если в запросе отсутствует JSESSIONID, либо он неправильный), 
то выполняется редирект на страницу ввода логина и пароля. Ввод логина и пароля в форму считается аутентификцией типа Form-Based, 
что означает, что имя и пароль приходят в POST-запросе в параметрах username и password (такие имена параметров по умолчанию 
используются в Spring Security). То есть когда пользователь попадет на страницу логина и вводит туда данные, на сервер 
пойдет новый запрос, в котором данные будут передаваться в этом самом POST запросе.

Чтобы задать аутентификацию типа Http Basic, в коде должна быть строка: *.and().httpBasic();

В этом случае браузеру придет ответ с требованием показать нативную браузерную форму, куда пользователь так же вводит данные. 
Но эти данные в случае Http Basic аутентификации передаются уже в другом виде - в заголовке, что небезопасно.

Но суть в том, что обе приведенные выше строки указывают Spring Security, как именно он должен брать из запроса имя пользователя и пароль.

Резюме: В данном приложении мы имеем несколько защищенных аутентификацией эндпоинтов (см. комментарии), два открытых и 
один доступный только для пользователей с определенной ролью, остальные доступны после аутентификации, см. код.

Основной интерес представляет файл конфигурации:
- [SecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_2/src/main/java/me/oldboy/config/SecurityConfig.java) - файл настройки системы защиты Spring-a;
- [application.properties](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_2/src/main/resources/application.properties) - файл свойств, откуда приложение берет настройки БД;
- [pom.xml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_3_2/pom.xml) - файл зависимостей;
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