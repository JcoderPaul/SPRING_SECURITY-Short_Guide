### Simple Spring Boot Security App (Part 7_1) - Настройка CORS, CSRF и еще раз немного о RememberMe.

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle
________________________________________________________________________________________________________________________
### Настройка Security CORS в Spring приложении.

В целях безопасности браузер запрещает JS-скрипту одного сайта обращаться на другой сайт без специального разрешения. 
Разрешение это реализуется с помощью технологии CORS (Cross-Origin Resource Sharing). Посмотрим, как это выглядит. У нас  
есть простое Spring Boot приложение с несколькими Rest-эндпоинтами, но для ускорения процесса мы будем обращаться к 
[NoticesController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/src/main/java/me/oldboy/controllers/api/NoticesController.java), доступ к нему открыт для всех в конфигураторе безопасности. Проверить это легко из браузера и PostMan-a.

А вот теперь давайте получим в браузере CORS ошибку. Для этого нам понадобится, или простой JS скрипт тискающий наш эндпоинт 
или, что по-сложнее - создадим Angular HTTP клиент (статика и js) именно он будет выполнять get-запрос к нашему контроллеру.
Мы сделали это для наглядности (а вообще хотели глянуть, как одна технология стыкуется с другой). 

И так, у нас есть:
- бэкэнд сервис на Spring по адресу http://localhost:8080 с точкой доступа /notices;
- фронтэнд HTTP клиент на JS(TS) по адресу http://localhost:4200; 

Повторюсь, можно взять любой js-фреймворк или просто страничку с JS-скриптом и дернуть эндпоинт нашего сервиса в качестве 
теста. Главное, что мы имитируем обращение с другого сайта. Важно, чтобы это был либо другой протокол, либо порт, либо URL. 
Браузер считает такие расхождения опасностью. И так, мы запускаем оба наших приложения, при этом мы еще не настроили CORS 
фильтр безопасности нашего Spring сервиса и он пугается буквально всего. После обращения через браузер на адрес 
http://localhost:4200 в панели разработчика мы увидим сообщение об ошибке примерного формата:

![CORS Error example.jpg](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/DOC/JPG/CORS/CORS%20Error%20example.jpg)

Страница, не отображается или отображается частично - вся статика, а вот данные из Spring сервиса не выводятся. Это работа 
CORS-политики браузера. Вот теперь, нам нужно включить и настроить CORS в Spring Security - проблема не в браузере см.
AppSecurityConfig.java. Добавляем соответствующий bean и прописываем в нем необходимые разрешения (адреса, методы: GET, PUT и т.д.).

Чтобы браузер не бросал ошибку, надо чтобы Spring приложение явно отдавало в http-ответе в заголовке Access-Control-Allow-Origin 
домен(ы), с которого разрешены запросы. То есть, чтобы не отсекать нашего клиента, запущенного на  http://localhost:4200, в ответе 
должен содержаться такой заголовок:

        Access-Control-Allow-Origin: http://localhost:4200

Подразумевается, что Spring сервис должен знать сайт http://localhost:4200 и давать ему разрешение на запросы с помощью 
вышеприведенного заголовка. Т.е. Spring приложение должно включать этот заголовок в response. Если бы наш JS клиент 
обращался к нашему Spring сервису с адреса test-spring-security-app.ru, то заголовок был бы таким:

        Access-Control-Allow-Origin: test-spring-security-app.ru

Есть универсальный и опасный вариант открыть доступ всем сразу и без разбору:

        Access-Control-Allow-Origin: *

Необходимые зависимости Spring Security у нас уже есть - остается включить CORS. В зависимости от версии применяемого 
фреймворка настройка конфигурации, цепочки фильтров безопасности, метода *.configure(), метода *.addCorsMappings(), 
будет отличаться, мы просто добавим нужный bean:

        @Bean
        UrlBasedCorsConfigurationSource corsConfigurationSource() {
            
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
            configuration.setAllowedMethods(Collections.singletonList("*"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            
            return source;
        }

Конфигурация говорит, что доступ и возможности будут следующими:
- только сайт http://localhost:4200 может делать запросы;
- запрос можно делать любыми методами (GET, POST, PUT и т.д.)
- обращаться к нашему приложению можно по любому внутреннему url —("/**")

Проверим, как себя чувствует наш Angular HTTP клиент. Перезапускаем Spring сервис и обновляем страницу браузера. В режиме 
разработчика мы видим, что запрос прошел нормально - ответ получен и в его заголовках есть ожидаемый результат см. ниже:

![CORS is configured on Spring BackEnd App.jpg](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/DOC/JPG/CORS/CORS%20is%20configured%20on%20Spring%20BackEnd%20App.jpg)

Процесс взаимодействия клиент-браузер-сервер:
- У HTTP запроса в заголовках Origin и Host содержится откуда и куда идет запрос. По ним приложение Spring поймет, надо 
ли включать в ответ заголовок Access-Control-Allow-Origin (если Origin и Host одинаковые, то не надо, проблемы в 
браузере в данном случае в не возникнет). Заголовок Origin браузер добавляет автоматически, JS-программист не 
может его подделать.

- Далее наше Spring приложение понимает, сайту http://localhost:4200 доступ разрешен (мы сами это настроили в Security 
конфигурации), так что в http-ответ оно включает "разрешающий" заголовок:

        Access-Control-Allow-Origin: http://localhost:4200

Браузер видит по этому заголовку, что сайту http://localhost:4200 доступ к серверу разрешен и возвращает нормальный ответ
клиенту вместо CORS ошибки, как делал раньше. Взаимодействие фронтэнд и бэкэнд приложений через браузер настроено.

________________________________________________________________________________________________________________________
### Генерация Security CSRF-токена в Spring приложении (самописные формы аутентификации и регистрации).

Теория CSRF (варианты атаки, способы защиты) прекрасно рассмотрена в разделе: [Lesson 111 - CSRF-Filter (CSRF атаки)](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_21#lesson-111---csrf-filter-csrf-%D0%B0%D1%82%D0%B0%D0%BA%D0%B8) и 
в [подборке статей к данному вопросу](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_21/DOC/CSRF). 
Тут мы сделаем упор на создание кастомных форм аутентификации, регистрации и настройке цепочки фильтров безопасности.

И так, на старте у нас есть файл конфигурации безопасности AppSecurityConfig.java в котором описана текущее состояние 
метода *.filterChain(), у нас есть:
- неактивная защита CSRF или httpSecurity.csrf(AbstractHttpConfigurer::disable);
- набор ссылок к которым разрешен доступ всем желающим или *.requestMatchers(...).permitAll();
- набор ссылок к которым разрешен доступ только аутентифицированным пользователям или *.requestMatchers(...).authenticated();
- набор ссылок к которым имеют доступ пользователи с определенной ролью или разрешением: *.requestMatchers(...).hasAuthority("ADMIN") или *.requestMatchers(...).hasRole("ADMIN");
- правила "хорошего тона" рекомендуют, не надеяться на память, а заканчивать цепочку "матчеров" следующей конструкцией: .anyRequest().authenticated();
- менеджер сессий Spring Security;
- метод управляющий работой RememberMe токеном или *.rememberMe();
- метод управляющий работой формой аутентификации *.formLogin();

Мы не будем проводить имитацию CSRF атаки, а просто посмотрим, как выглядит код формы без CSRF защиты в консоли разработчика 
браузера, пока у нас защита отключена и естественно отсутствуют какие-либо упоминания в коде форм о CSRF:
![см. форма аутентификации](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/DOC/JPG/CSRF/No_CSRF_Security_LoginForm.jpg)

________________________________________________________________________________________________________________________

![см. форма регистрации](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/DOC/JPG/CSRF/No_CSRF_Security_RegForm.jpg)

Теперь мы убрали из цепочки фильтров метод явно дезактивирующий CSRF защиту и снова посмотрим, как будет выглядеть код 
формы в консоли разработчика (т.е. CSRF защита включена): 
![см. форма аутентификации](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/DOC/JPG/CSRF/With_CSRF_SecurityToken_LoginForm.jpg)

________________________________________________________________________________________________________________________

![см. форма регистрации](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/DOC/JPG/CSRF/With_CSRF_SecurityToken_RegForm.jpg)

Как и в случае с CORS, включение защиты с помощью CSRF-токена идет в Spring Security "из коробки", если мы сами явно не 
отключаем его в цепочке фильтров безопасности. Теперь когда пользователь аутентифицируется в нашем приложении (сервисе), 
ему выделяется специальный CSRF-токен. Он хранится в сессии и должен отправляться как скрытое поле, в идеале, со всех 
форм (также он должен прилагаться в XMLHttpRequest-запросах PUT, DELETE, POST - это для JavaScript). В статьях ссылки на
которые указаны выше описаны классические CSRF атаки, но возможны и более сложные атаки. И хотя браузеры становятся все 
более безопасными, как и сам Spring MVC, все равно при реализации форм для PUT, POST, DELETE-запросов (т.е. изменяющих), 
необходимо генерировать CSRF-токен.

Естественно, настройками CSRF-токена можно управлять - убрать некоторые методы или некоторые url - сделать так, чтобы 
для них не требовался токен.

Итак, токен в POST-запросах сейчас требуется. Если оставить все как есть, то наше приложение не заработает - при попытке 
отправить форму, мы ответ получим 403. Надо сделать так, чтобы выданный CSRF-токен отправлялся. Для этого необходимо 
добавить в форму(ы) скрытое поле, либо "руками":

    <form ... >
        <!--csrf-токен добавить на форму-->
        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>
        ...
    </form>

Второй вариант использовать шаблонизатор Thymeleaf и его синтаксис - th:action="@{...endpoint url...}":

    <form th:action="@{/webui/login}" class="form-signin" method="post" >
        ... some code ...
    </form>

Для того чтобы такая форма сработала и CSRF-токен добавился в наши формы необходимо подключить к нашему проекту пару зависимостей:

    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation "org.thymeleaf.extras:thymeleaf-extras-springsecurity6:3.1.3.RELEASE"

Теперь и наш сервис работает под CSRF защитой, и отправка подменной формы с мошеннического сайта не будет иметь прежнего 
успеха, потому что CSRF-токен генерируется нашим сервисом (сервером) и "злые люди" не могут его подделать. В отличие от 
JSESSIONID, он не хранится в куки браузера и не отправляется автоматически при запросах на наш сервис (сайт банка и т.п.).
________________________________________________________________________________________________________________________
### Еще раз о настройке Remember-Me Authentication и управление сессиями.

В цепочке фильтров у нас есть несколько методов определяющих работу логин формы, RememberMe-токена и самое интересное 
создание Security сессий:

    .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
    .rememberMe((remember) -> remember.rememberMeParameter("remember-me")
                                      .tokenRepository(tokenRepository()))
    .formLogin(login -> login.loginPage("/webui/login")
                             .defaultSuccessUrl("/webui/hello"))

Spring security предоставляет различные возможности для управления созданием сессии. Он предоставляет нам возможность 
настроить, когда будет создана сессия и как мы можем взаимодействовать с ней. Вот доступные в security опции, которые 
могут помочь нам настроить и контролировать создание сессии:

- SessionCreationPolicy.ALWAYS - сессия создается всегда (если она не существует).
- SessionCreationPolicy.NEVER - Spring Security не создает Http Session в принципе, а будет использовать уже готовую Http Session, если она уже существует (доступно через сервер приложений).
- SessionCreationPolicy.IF_REQUIRED - Spring Security создаст Http Session только при необходимости (конфигурация по умолчанию - "by default" Spring Security будет использовать эту опцию без специального указания).
- SessionCreationPolicy.STATELESS - Spring Security никогда не создаст Http Session и никогда не будет использовать ее для получения SecurityContext.

Для "login based" приложений, SessionCreationPolicy.IF_REQUIRED используется в большинстве случаев и также является 
значением по умолчанию в Spring Security - для типичного веб-приложения. Чтобы изменить политику создания сессии в 
Spring Security, мы можем переопределить метод configure, переопределив WebSecurityConfigurerAdapter или, как в нашем 
случае метод *.filterChain().

#### Важные моменты:

1. Эти конфигурации управляют только поведением безопасности Spring, но не нашим приложением. Наше приложение может 
использовать другие конфигурации создания сессий.
2. По умолчанию Spring security создаст сессию, когда это необходимо. Она может использовать сессию, созданную нашим 
приложением вне контекста Spring security (помним, что сессии создаются сервером приложений, даже если он встроен).
3. STATELESS гарантирует, что Spring Security не создаст ни одной сессии, однако это не означает, что наше приложение 
в принципе не создаст ни одной сессии. SessionCreationPolicy.STATELESS - применяется только к контексту безопасности 
Spring. Мы можем увидеть JSESIONID в приложении, и это не значит, что конфигурация безопасности Spring не работает.

Помним, что Spring Security обрабатывает запросы на вход и выход с помощью HTTP Session. Т.е. при политике - SessionCreationPolicy.STATELESS - 
Spring Security не будет использовать файлы cookie, и каждый запрос потребует повторной аутентификации. 

Как это выглядит на практике. Если запустить наше приложение, с текущей конфигурацией безопасности, а форме аутентификации
после ввода своих данных не ставить галочку Remember Me, получим интересный эффект - мы увидим JSESSIONID, но войти в 
приложение не сможем (по крайней мере, на защищенные страницы), система безопасности просто не знает как аутентифицировать 
пользователя (Security сессии нет, authentication-a дергать не откуда) см. ниже: 

SessionCreationPolicy_STATELESS.jpg

Однако стоит поставить галочку в поле Remember Me и мы успешно проходим на страницу приветствия с отображением Login-a, 
а также Cookies remember-me:

SessionCreationPolicy_STATELESS_RememberMe.jpg

Данный эффект был описан в разделе "Настройка Remember-Me аутентификации" ReadMe файла предыдущей части. Обычно политику -
SessionCreationPolicy.STATELESS применяют при аутентификации с токенами.
________________________________________________________________________________________________________________________
#### Особенности настройки отображения. 
У нас есть Spring Boot приложение в котором мы используем кастомные страницы аутентификации и регистрации, а для 
презентабельного отображения оных мы применяем файлы стилей CSS. Особенностью является место расположения страниц 
отображения - папка templates, и место хранения файлов стилей - папка static. Все это находится в папке ресурсов - resources.

Так же важным аспектом является настройка Spring Security так, чтобы он давал доступ к файлам стилей CSS (а то красоты не будет):

    *.requestMatchers(antMatcher("/css/**")).permitAll()

________________________________________________________________________________________________________________________
### Reference Documentation:

* [CSRF](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html)
* [Cross Site Request Forgery (CSRF)](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
* [CORS Servlet Applications](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
* [CORS Reactive Applications](https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html)
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

* [CSRF Protection in Spring Security](https://www.geeksforgeeks.org/csrf-protection-in-spring-security/)
* [A Guide to CSRF Protection in Spring Security](https://www.baeldung.com/spring-security-csrf)
* [Spring Security - CORS](https://www.geeksforgeeks.org/spring-security-cors/)
* [CORS with Spring](https://www.baeldung.com/spring-cors)
* [Spring Security – UserDetailsService and UserDetails with Example](https://www.geeksforgeeks.org/spring-security-userdetailsservice-and-userdetails-with-example/)
* [@Valid Annotation on Child Objects](https://www.baeldung.com/java-valid-annotation-child-objects)
* [Java Bean Validation Basics](https://www.baeldung.com/java-validation)
* [Javax validation on nested objects - not working](https://stackoverflow.com/questions/53999226/javax-validation-on-nested-objects-not-working)
* [Проверка данных — Java & Spring Validation](https://habr.com/ru/articles/424819/)
________________________________________________________________________________________________________________________