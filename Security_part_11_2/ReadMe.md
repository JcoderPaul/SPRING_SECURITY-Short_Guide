### Simple Spring App with Security (Part 11_2) - применение OAuth2 в защите приложений (продолжение).

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle
________________________________________________________________________________________________________________________
Повторение прошлого, в представленных материалах достаточно подробно рассмотрен текущий вопрос:
- [Spring Security Starter и OAuth-2.0 (Теория)](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_22#oauth-20-%D1%82%D0%B5%D0%BE%D1%80%D0%B8%D1%8F);
- [Технология OAuth 2.0](https://github.com/JcoderPaul/Spring_Framework_Lessons/blob/master/Spring_part_22/DOC/OAuth_2_0/AuthorizationWithOAuth_2_0.txt);
- [OAuth Grant Types](https://oauth.net/2/grant-types/);
________________________________________________________________________________________________________________________
### Еще немного теории.

Нужно еще раз повториться про то, какие части "головоломки" участвуют в OAuth2 аутентификации, а еще точнее, что с чем 
и как взаимодействует для получения "требуемого":
- **Resource Owner** - потребитель данных (или user, или resource owner - "собственник ресурса"), обычно с учетными данными (пароль/логин);
- **Resource Server** - хранилище ресурсов (некая БД, файловый архив, сервер ресурсов, back-end) с некой пропускной системой (необязательно по паролю и логину);
- **Authentication Server** - "страж перевала", система безопасности, сервер определяющий кому и при каких условиях можно 
выдавать некие "разрешительные грамоты с ограниченным сроком действия". Он сам по себе и занимается только раздачей этих 
самых "грамот" - "access token-ов". Разрешение на допуск к ресурсу выдаются на основании "документов" (в простом случае, 
тех же паролей/логинов, более сложном, неких кодов доступа), подтверждающих права на запрашиваемые ресурсы.

Но, в перечисленном выше списке, "собственник ресурса" - resource owner, обычно действует через посредника - клиентское 
приложение или "Client application" (приложение на телефоне воспринимается проще, т.к. выглядит "самостоятельным", 
нежели web-приложение развернутое в браузере). Web-приложению для отображения данных пользователю нужен интерфейс или 
средство отрисовки (возврата) запрашиваемых данных, и обычно web-приложение в качестве средства (отрисовки) возврата данных
использует браузер. И тут возникают два понятия, которые периодически смешивают друг с другом:

- **Client application** - посредник, между "Resource owner" и "Resource server" (пользователь и источник данных), который 
будет возвращать данные пользователю в удобочитаемом виде. Обычно это некий front-end, который берет на себя также функционал
по взаимодействию между "Resource Server" и "Authentication Server", на основании полученных от пользователя "подтверждений"
своей личности.
- **User agent** - очень хитрый дополнительный термин (применяется не всегда и не везде), т.к. выше мы уже упоминали про 
мобильные приложения и там работа клиентского приложения или просто клиента (именно "клиента", а не "пользователя" - 
собственника ресурса) обычно выглядит "условно" самостоятельно, то в web-приложении для работы очень часто используется 
web-браузер - именно он и является "User agent" - это способ взаимодействия клиентского приложения Client application и 
Resource Owner. Т.е. у нас может быть, например, еще один сервер (на Node.js) на котором "крутится" клиентское приложение - 
Client application (сам код приложения, его местонахождение), а его работу отрисовывает web-браузер - User agent.

Дело в том, что место формирования (хранения) front-end client application влияет:
- на настройки сервера авторизации (public или confidential);
- выбор способа взаимодействия всех четырех основных частей в цепочке (flow) безопасности. 

Например, при не верном выборе security flow, может возникнуть ситуация в которой секретные данные окажутся легкодоступными 
для "плохишей". 

![OAuth2_Scheme](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/OAuth2_Scheme.jpg)

________________________________________________________________________________________________________________________
### Часть 4. - Применение KeyCloak в качестве OAuth2 сервиса авторизации.

Теперь нужно настроить, как и раньше, все элементы цепи безопасного взаимодействия см. рис. выше.

#### Настроим KeyCloak сервис:

Рассмотрим два варианта развертывания нашего сервиса авторизации: 

- [Вариант 1. - установка KeyCloak на локальную машину, настройка и запуск](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/KeyCloak_on_local_PC.md);
- [Вариант 2. - развертывание KeyCloak в Docker контейнере, настройка и запуск](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/KeyCloak_run_on_Docker.md);

#### Настроим наше "клиентское" приложение (Client App):

После того как мы развернули и настроили сервис аутентификации KeyCloak, нам нужно настроить наше приложение. Пропишем 
конфигурацию в application.yaml, добавим указатель на настроенный в KeyCloak realm, как провайдер, и "зарегистрируем" 
нового поставщика OAuth2:

        security:
          oauth2:
            client:
              provider:
                keycloak:
                  issuer-uri: http://localhost:9100/realms/SpringSecProject-OAuth-Test-Realm
              registration:
                keycloak:
                  clientId: SpringSecProject-OAuth-Test-Client
                  clientSecret: c2jKqqHSUOE8JWCRVAEDsf2MeViuYCS1
                  redirectUri: http://localhost:8080/login/oauth2/code/keycloak
                  scope: openid,profile

________________________________________________________________________________________________________________________
##### Важный момент! Может понадобиться настройка порта нашего приложения в application.yml, на случай если KeyCloak развернут "by default" на порту 8080, например: server.port: 8085
________________________________________________________________________________________________________________________

В данном случае мы применяем наше приложение, как единый клиент-ресурс-сервис и зависимостей, что мы добавили ранее, хватает:

        implementation 'org.springframework.security:spring-security-oauth2-client'

Тут есть интересный нюанс, дело в том, что когда мы настраивали аутентификацию Google, мы создали метод *.oidcUserService() 
в классе AppSecurityConfig.java, который возвращает OidcUser, причем "на наших условиях", т.е. ответ полученный после 
аутентификации, либо на Google, либо на нашем KeyCloak сервисе "разбирается", из него "выдергивается" email и уже затем 
из БД самого приложения извлекается все необходимое, например права и роли ("ROLE_"). Т.е. роли прописанные в ключах 
доступа или tokenId, те, что заданы у аутентифицированного пользователя в базе сервиса аутентификации (Google, GitHab, 
KeyCloak), просто игнорируются и не попадают в цепочку фильтров, для определения прав доступа к ресурсам и привилегий.

________________________________________________________________________________________________________________________
##### Важный момент! Метод logout цепи безопасности в нашем AppSecurityConfig.java настроен "примитивно" и решает задачу "разлогинивания" как и полагается, однако, для обработки процесса "OIDC logout" нужны несколько другие механизмы.
________________________________________________________________________________________________________________________

Теперь, мы можем протестировать наше приложение на нескольких версиях развернутого KeyCloak-a и локально на PC и в Docker 
контейнерах, нужно только помнить, что Secret Key у этих версий разный (может быть сгенерирован заново, но не подставлен
нами в сервер аутентификации по желанию), т.е. следим за структурой данных в application.yml.

Естественно, варианты аутентификации через Google, GitHub, и простой пароль/логин в нашей форме продолжают работать.  

________________________________________________________________________________________________________________________
### Key Class and Methods:

* [org.springframework.security.access.prepost](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/package-summary.html)

* [Class HttpSecurity](https://docs.spring.io/spring-security/site/docs/5.0.0.M5/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(javax.servlet.Filter,%20java.lang.Class))
* [Interface HttpSecurityBuilder](https://docs.spring.io/spring-security/site/docs/4.1.0.RC2/apidocs/org/springframework/security/config/annotation/web/HttpSecurityBuilder.html)

### Reference Documentation:

* [EnableMethodSecurity](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/method/configuration/EnableMethodSecurity.html)
* [PreFilter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PreFilter.html)
* [PostFilter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PostFilter.html)
* [PostAuthorize](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PostAuthorize.html)
* [PreAuthorize](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PreAuthorize.html)

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

* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2)
* [OAuth 2.0 Login - Advanced Configuration](https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/oauth2login-advanced.html)

* [Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
 
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Spring Guides](https://spring.io/guides)
* [Spring Boot Security Auto-Configuration](https://www.baeldung.com/spring-boot-security-autoconfiguration)
* [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)
* [Spring Security: Upgrading the Deprecated WebSecurityConfigurerAdapter](https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter)

### Articles (question-answer):

* [Introduction to Spring Method Security](https://www.baeldung.com/spring-security-method-security)
* [Spring @EnableMethodSecurity Annotation](https://www.baeldung.com/spring-enablemethodsecurity)

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

### Old material (repeat):

* [Spring Boot lessons part 22 - Security Starter - PART 3](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_22)
________________________________________________________________________________________________________________________