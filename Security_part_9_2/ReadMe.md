### Simple Spring Boot App with Security (Part 9_2) - применение JWT токена (продолжение).

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle
________________________________________________________________________________________________________________________
### Часть 3. - Пример приложения с JWT-токеном (девиантный подход, продолжение и окончание).

И так, ранее мы уже определились в каких ситуациях принято применять JWT token, а в каких лучше этого не делать, т.к. это
рушит всю задумку такового. Но мы исследователи и наша задача исследовать странные нестандартные подходы, искать новые,
возможно и бесполезные приемы в работе новых для нас технологий и приемов. Продолжим!

Ранее, для хранения и передачи информации о клиенте (т.к. у нас монолит, который соединил в себе back-end и front-end блоки)
мы использовали сессию, в которую закидывали данные о email-е аутентифицированного клиента. Т.е. в определенный момент времени,
мы помещали JWT token в класс хранящий не синхронизированную Map: email-token. Далее в нужный момент из сессии извлекался 
ключевой email, а по нему, ранее сгенерированный и сохраненный токен из класса хранителя, а точнее при каждом обращении к
какой-либо странице приложения (в REST сервисах это были бы endpoint-ы). При штатном выходе из приложения (сервиса) - logout,
сохраненный JWT токен удалялся из Map, что не возможно при стандартном протоколе работы с JWT token-ом.

Теперь попробуем тот же "фокус" провернуть при использовании cookie-s, мы помним, что хоть у нас и *.sessionCreationPolicy(STATELESS)
режим работы контекста безопасности, но session и cookie созданные в недрах приложения мы использовать можем легко и просто.
Используем эту возможность. Тут мы не будем подробно расписывать, что и в каких классах у нас находится - сделано ранее. 
Структура и названия классов остались теми же - чуть изменилась логика и код тех классов, что отвечают за обработку запросов и 
ответов, т.к. теперь мы работаем с cookie-s.

Опишем логику работы ключевых моментов:
- Шаг 1. - клиент обращается к web интерфейсу сервиса любой endpoint (страница) - http://localhost:8080/webui/ ;
- Шаг 2. - поскольку у нас активна система безопасности сервис перебрасывает клиента на страницу (endpoint) аутентификации - http://localhost:8080/webui/login (работает метод *.getLoginPage() класса LoginRegController и view форма login.html);
  - Шаг 2.1 - если пользователь еще не зарегистрирован, он может зарегистрироваться в сервисе перейдя на - http://localhost:8080/webui/registration (работает метод *.regClientPage() класса LoginRegController и view форма registration.html) после процедуры регистрации он автоматически вернется в форму аутентификации;
  - Шаг 2.2 - если пользователь ранее уже регистрировался - просто вводит учетные данные и ... дальше начинается самое интересное;
- Шаг 3. - сервис проверят учетные данные (если данные не верны в форме аутентификации просто появиться сообщение об этом), если же все в порядке, то клиент переходит на - http://localhost:8080/webui/jwt_token (работает метод *.getJwtAndContinue() класса LoginRegController и view форма continue.html);

На третьем шаге нашему back-end сервису приходится заняться работой front-end сервиса (приложения) и сохранить сгенерированный
JWT токен (работает метод *.saveJwtToken() класса JwtSaver). 

Теперь самое интересное - процесс передачи запроса от клиента к сервису и процесс формирования ответа от сервера клиенту. 
Если взять классический REST сервис, то нам нужно сформировать некий запрос (например через браузер, POSTMAN) и отправить 
его к нужному endpoint-у, и обычно, если это GET запрос, мы просто обращаемся к определенному URL с указанием параметров 
(или без них) и далее сервер (сервис, приложение) дают четкий однозначно идентифицируемый ответ. В ситуации же когда мы 
обращаемся к странице с визуальным отображением запрошенной информации на один запрос мы можем получить не простой, а 
комплексный ответ (состоящий из набора запросов и ответов, нужный для правильного отображения информации, нашего единственного 
запроса). 

Про что это мы, все просто, допустим мы хотим чтобы клиент, на его запрос, получил страницу логина (адреса и т.п.), хорошо 
отобразим. Но при формировании отображения могут участвовать таблицы стилей CSS, картинки и т.п., значит понадобятся еще 
запросы и ответы для того, чтобы собрать все в один блок и отобразить клиенту. Значит через фильтр безопасности Spring 
Security будут пропущены все запросы! Если вдруг мы сами отключили (или этого требует логика проекта) session и cookie, то 
нужно понимать, что на каком-то этапе прохождения security filter chain мы можем "потерять" данные аутентификации. Такого 
не произойдет если пара запрос-ответ имеют простую форму, т.е. back-end выполняет только свои функции и ничего более: 
получил запрос -> обработал (прогнал по цепи фильтров) -> сформировал ответ -> вернул. Ну или используется другой способ 
аутентификации. 

Но у нас JWT и мы его используем совсем нестандартно. И так продолжим:

Если учетные данные верны происходит следующее (где-то в глубинах третьего шага):
- Шаг 3.1 - в момент проверки данных в цепи фильтров безопасности подключается UsernamePasswordAuthenticationFilter и проделывает необходимые операции проверки;
- Шаг 3.2 - в момент успешной аутентификации наш перехватчик процесса AuthenticationEventListener отображает результат в консоли и самое главное - запоминает учетные данные (без пароля), поскольку у нас нет сессии их надо где-то хранить;
- Шаг 3.3 - сохраненные данные об аутентификации попадают в наш фильтр генератор JWT - JwtTokenGeneratorAndAfterFilter.java;
- Шаг 3.4 - фильтр JwtTokenGeneratorAndAfterFilter генерирует JWT токен, а также создает cookie "Email" куда заносит email текущего аутентифицированного клиента.
- Шаг 3.5 - после генерации фильтр возвращает в response клиенту в заголовке "Authorization" значение token-a, все это попадает в метод *.getJwtAndContinue() класса LoginRegController;
- Шаг 3.6 - в методе *.getJwtAndContinue() из полученного в header-a мы извлекаем token, парсим его, извлекаем email клиента и сохраняем в хранителе токенов - JwtSaver;

На странице отображения клиент видит полученный JWT token и может продолжать работу. Поскольку мы старались сохранить
логику взаимодействия front-end и back-end модулей, как будто они самостоятельные части, то далее происходит следующее:
- Шаг 4. - со страницы continue.html методом POST мы обращаемся к endpoint-у "/main";
- Шаг 5. - запрос проходя через фильтры попадает в SetRequestHeaderFilter, где мы извлекаем из cookie "Email" значение email клиента;
  - Шаг 5.1 - далее, тут же в SetRequestHeaderFilter, по извлеченному email мы получаем из хранителя токенов ранее сохраненный на шаге 3.6 JWT token;
  - Шаг 5.2 - значение токена перебрасывается из response в request и уходит следующему фильтру;
- Шаг 6. - теперь в цепи фильтров подключается наш фильтр JWT валидатор - JwtTokenValidatorAndBeforeFilter, который достает из полученного request header-a "Authorization" ранее полученный JWT token, парсит его и устанавливает объект аутентификации в контекст безопасности;
- Шаг 7. - клиент получает доступ к требуемой странице (нужному endpoint-у);

Цикл при каждом обращении повторяется в плоть до перехода на стр. (endpoint) logout. Тут мы обнуляем ранее установленную 
cookie и удаляем из хранителя токен по ключу (email). Хотя, если капнуть глубже удаленный токен продолжает работать и 
реализуй мы функционал при котором с запросом мы бы передавали полученный token "руками, через некую форму", или реализовали
логику обработки token-a по-другому, то при прочих равных, он прошел бы проверку и дал допуск с сервису в плоть до момента 
истечения его актуальности.  
________________________________________________________________________________________________________________________
### Key Class and Methods:

* [Class HttpSecurity](https://docs.spring.io/spring-security/site/docs/5.0.0.M5/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(javax.servlet.Filter,%20java.lang.Class))
* [Interface HttpSecurityBuilder](https://docs.spring.io/spring-security/site/docs/4.1.0.RC2/apidocs/org/springframework/security/config/annotation/web/HttpSecurityBuilder.html)

### Reference Documentation:

* [JSON Web Token (JWT)](https://www.rfc-editor.org/rfc/rfc7519.html)
* [JSON Web Token](https://en.wikipedia.org/wiki/JSON_Web_Token)

* [Spring Security Filter Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
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

* [JSON Web Token Claims](https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-token-claims)
* [Пять простых шагов для понимания JSON Web Tokens (JWT)](https://habr.com/ru/articles/340146/)
* [JWT Security Best Practices](https://curity.io/resources/learn/jwt-best-practices/)

* [A Custom Filter in the Spring Security Filter Chain](https://www.baeldung.com/spring-security-custom-filter)
* [Путешествие к центру Spring Security](https://habr.com/ru/articles/724738/)
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