### Simple Spring Boot Security App (Part 6_1) - Remember-Me Authentication.

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle
________________________________________________________________________________________________________________________

Долгие сессии могут нагрузить сервер, так как все объекты пользовательских сессий хранятся в куче контейнера. Поэтому 
сессии имеет смысл сделать короче, а идентичность пользователя запоминать с помощью специального долгосрочного Hash-Based 
токена. Он содержит только имя пользователя и хэш, с помощью которого можно проверить подлинность токена. При этом 
истекшие сессии не восстанавливаются, а начинаются заново. Зато пользователь может не совершать заново вход в систему - 
его помнят благодаря Remember-Me токену. 

Есть два вида Remember-Me токенов:
- [Simple Hash-Based Token](https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html#remember-me-hash-token);
- [Persistent Token](https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html#remember-me-persistent-token);

Обычно Hash-Based Token идет по умолчанию и с ним проще работать, достаточно настроить Spring Security config файл и 
его цепочку фильтров - [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_6_1/src/main/java/me/oldboy/config/AppSecurityConfig.java) - достаточно указать (в новых версиях Spring):

    .rememberMe(Customizer.withDefaults())

И в стандартной форме аутентификации появиться checkbox элемент - remember-me. Нам более интересен вариант работы с 
Persistent Token, т.к. он сохраняется в БД - его мы и будем рассматривать далее. 
________________________________________________________________________________________________________________________

И так продолжим. Сессии придуманы для того, чтобы сервер «помнил» пользователя при повторных запросах от него. То есть 
пользователь вводит однократно имя и пароль, и при дальнейших запросах сервер понимает, от кого именно пришел запрос, 
а также какие объекты есть в данном сеансе (например, товары в корзине покупок).

Пояснить взаимодействие с сервером можно на примере обращения в службу поддержки. При первом обращении клиент описывает 
проблему и получает номер обращения (JSESSIONID). Дальше переписка идет под этим номером обращения. Клиенту не надо 
каждый раз заново все пересказывать. Служба поддержки (сервер) по номеру сама восстанавливает все детали (идентичность 
пользователя и данные сессии). Реализуется это с помощью идентификаторов сессий. 

Стандартный алгоритм следующий:
- Сервер высылает клиенту при первом запросе (например, при успешном логине, но можно и анонимному клиенту) заголовок 
типа:

        Set-Cookie: JSESSIONID=4C7871D1EF406F69C7CF20CD6BD283F1

- Браузер сохраняет эти значения (свои для каждого сайта), и далее при каждом запросе на конкретный сайт браузер 
автоматически добавляет к запросу соответствующий заголовок, полученный от сервера:

        Cookie: JSESSIONID=4C7871D1EF406F69C7CF20CD6BD283F1

Название JSESSIONID не универсально, а характерно именно для Java серверного приложения. В других языках используются 
другие названия. При последующих запросах от того же клиента сервер или контейнер сервлетов (например Apache Tomcat) 
опознает клиента по идентификатору сессии. Контейнер хранит эти идентификаторы сессий и соответствующие данные клиента 
как словарь - Map:

        Ключ JSESSIONID (конкретный идентификатор) : данные сессии

Сессия имеет срок жизни. Как только он истекает, данные исчезают, и в последующих запросах контейнер не принимает 
истекший Cookie конкретного клиента. Сессии исчезнут, если перезапустить Tomcat, так как они хранятся в «куче» Tomcat-а.

        По умолчанию в Apache Tomcat сессия уничтожается после 30 минут бездействия клиента.

Помним:
- Сессии работают и без Spring приложения (сервиса), это особенность работы web-сервера (контейнера сервлетов), 
программы реализующей интерфейс HttpSession, например наш Apache Tomcat.
- Remember-Me аутентификация - это особенность Spring приложения использующего Security фреймворк.
________________________________________________________________________________________________________________________

И так, добавляем в цепочку фильтров метод *.rememberMe(Customizer.withDefaults()) и после ввода пароля и логина 
активируем флажок - запомнить меня. Создается Remember-Me токен, что сразу видно, если в браузере зайти в инструменты 
разработчика. Токен позволяет помнить пользователя и после того, как срок годности сессии истечет, а также после 
перезапуска сервера, если токен хранится в БД сервиса (в данном случае - нашего приложения).

Токен высылается клиенту в Set-Cookie аналогично сессии. Но восстановить из него можно только имя пользователя, никакие 
другие данные по нему не восстанавливаются — хранить в нем объекты нельзя (а в сессии можно). При каждом запросе 
автоматически выполняется проверка подлинности токена.

И если серверов несколько, то Remember-Me аутентификация будет работать, так как она не завязана на конкретный 
Tomcat-контейнер (и хранящуюся в его памяти сессию). Опознание пользователя происходит не путем обращения в Map 
в куче конкретного запущенного сервера (где содержатся ключи JSESSIONID и соответствующие данные сессии, среди 
которых имя пользователя), а по другому — путем проверки подлинности токена.

        По умолчанию Remember-Me помнит пользователя две недели.
________________________________________________________________________________________________________________________
### Hash Based токен и проверка подлинности - теория.

Как уже писалось ранее в Remember-Me аутентификации можно выбрать два вида токенов: 
- Simple Hash-Based Token;
- Persistence Token (хранится в базе) - его мы и используем;

Обычно Hash Based токен содержит:
- имя пользователя и срок годности токена в открытом виде (почти открытом — Base64);
- некий хеш (md5Hex) - значение, вычисляемое на основе имени, пароля, срока годности токена и секретного ключа;

Вычисляется он так: md5Hex(username + ":" + expirationTime + ":" password + ":" + key)

Весь токен такой:
    
    base64(username + ":" + expirationTime + ":" +
    md5Hex(username + ":" + expirationTime + ":" password + ":" + key)), где
    username:          As identifiable to the UserDetailsService
    password:          That matches the one in the retrieved UserDetails
    expirationTime:    The date and time when the remember-me token expires, expressed in milliseconds
    key:               A private key to prevent modification of the remember-me token

Из этого хеша md5Hex пароль обратно не восстановить, но на бэкенде можно по доступному из токена имени найти пароль и 
вычислить хеш заново. Приложение так и делает - каждый раз когда токен приходит, оно находит по имени пароль, вычисляет 
md5Hex, убеждается что он совпадает с полученным и выносит вердикт - является ли пользователь тем, за кого себя выдает.

Теоретически клиент каждый раз мог бы просто высылать имя и пароль, а приложение каждый раз так же находить по имени 
пароль и проверять, совпадает ли он. Но пароль каждый раз передавать опасно. Суть в том, чтобы передавать именно хеш 
из которого невозможно извлечь данные, но, на которых он построен. То есть он работает в одну сторону. Зная данные 
(у нас это имя, пароль, срок годности и секретный ключ), хэш можно просто пересчитать, а не пытаться получить из хэша
данные обратно. Т.е хотя в браузере и виден токен, пароль из него не извлечь.
________________________________________________________________________________________________________________________
### Настройка Remember-Me аутентификации.

Как уже упоминалось ранее добавить Remember-Me токен можно следующим образом:

        @Bean
        @SneakyThrows
        public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
            httpSecurity.csrf(AbstractHttpConfigurer::disable)
                        .cors(AbstractHttpConfigurer::disable)
                              /* some filter method */
                        .httpBasic(Customizer.withDefaults())
                        .formLogin(Customizer.withDefaults())
                        .rememberMe((remember) -> remember.tokenRepository(tokenRepository()));

		    return httpSecurity.build();
	    }

При такой настройке будут использоваться как сессии, так и Hash-Based токен. Токен продолжит действовать, когда 
сессия истечет, но данные из сессии (если они есть) будет уже не извлечь. Чтобы задать срок действия токена например 
24 часа можно добавить:

      .rememberMe((remember) -> remember.tokenRepository(tokenRepository()).tokenValiditySeconds(86400));

Можно отключить сессии и использовать только токен, тогда в инструментах разработчика мы не увидим JSESSIONID, а только 
remember-me:

      .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
      .rememberMe((remember) -> remember.tokenRepository(tokenRepository()).tokenValiditySeconds(86400));

Чтобы задать наш секретный ключ, можно применить *.key("mySecretKey"), в реальности, вместо "mySecretKey" в метод,
естественно, передается некий секретный ключ:

      .httpBasic(Customizer.withDefaults())
      .formLogin(Customizer.withDefaults())
      .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
      .rememberMe((remember) -> remember.tokenRepository(tokenRepository())
                                        .tokenValiditySeconds(86400)
                                        .key("mySecretKey"));

Если ключ не задать, он генерируется автоматически. Также можно сделать создание токена обязательным, независимо от 
того, включен ли на форме флажок remember-me:

      .alwaysRemember(true)

При отключенных сессиях так и надо делать (а флажок на форме убрать), потому что тогда Remember-Me токен остается 
единственным способом идентифицировать пользователя при последующих запросах, а значит, он должен быть обязательным.
________________________________________________________________________________________________________________________
Основные обязательные настройки приведены в [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_6_1/src/main/java/me/oldboy/config/AppSecurityConfig.java). 

Скрипт таблицы для хранения хэш-токена приведен тут - [persistent_logins_token.sql](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_6_1/DOC/persistent_logins_token.sql), его так же легко найти в официальной документации [см. Remember-Me Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html#remember-me-hash-token).
________________________________________________________________________________________________________________________
В данном приложении мы перешли на миграционный фреймворк [Liquibase](https://docs.liquibase.com/) и теперь [таблицы и стартовые данные](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_6_1/DOC/scripts.sql)
будут разворачиваться в нашей БД при первичном его запуске. Что самое интересное, при запуске тестов мы можем развернуть 
тестовую БД в контейнере и средствами Liquibase прогрузить туда наши таблицы и данные из ["рабочих" скриптов](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_6_1/src/main/resources/db/changelog).
Что удобно, т.к. в тестовой БД у нас будет полная копия рабочей БД.

Но на этот раз мы поступим по-старому, выгрузим данные в Testcontainer тестовые данные из тестовых ресурсов - [data.sql](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_6_1/src/test/resources/sql_scripts/data.sql),
для проверки интеграционного взаимодействия слоев приложения.

В текущих тестах мы два класса тестировали в различных вариациях с условным замером времени таковых:
- [ac_test_config_mod](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_6_1/src/test/java/me/oldboy/unit/controllers/ac_test_config_mod) - два варианта (с @AutoConfigureMockMvc и @WebMvcTest) на класс [AccountController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_6_1/src/main/java/me/oldboy/controllers/AccountController.java);
- [cc_test_config_mod](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_6_1/src/test/java/me/oldboy/unit/controllers/cc_test_config_mod) - 3-и варианта ("полная изоляция", авто-конфигурация и @WebMvcTest) на класс [ClientController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_6_1/src/main/java/me/oldboy/controllers/ClientController.java);

Полный комплект тестов (покрытие Class 100%(58/58), Method 87%(170/194), Lines 90%(370/408)) - [test](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_6_1/src/test)
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