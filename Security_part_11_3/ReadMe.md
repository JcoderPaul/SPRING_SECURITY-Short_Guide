### Simple Spring App with Security (Part 11_3) - применение OAuth2 в защите сервиса ресурсов (окончание).

- Spring 6.2.2 (nonBoot)
- Spring Security 6.4.2
- Java 17
- Gradle
________________________________________________________________________________________________________________________
Повторение прошлого, в представленных материалах достаточно подробно рассмотрен текущий вопрос:
- [Spring Security Starter и OAuth-2.0 (Теория)](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_22#oauth-20-%D1%82%D0%B5%D0%BE%D1%80%D0%B8%D1%8F);
- [Технология OAuth 2.0](https://github.com/JcoderPaul/Spring_Framework_Lessons/blob/master/Spring_part_22/DOC/OAuth_2_0/AuthorizationWithOAuth_2_0.txt);
- [OAuth Grant Types](https://oauth.net/2/grant-types/);
________________________________________________________________________________________________________________________
### Еще немного теории.

Все наши предыдущие приложения взаимодействуя с каким либо сервисом авторизации являлись, как бы одновременно и сервисом
данных и клиентским приложением (т.е. отображали информацию в неком "красивом виде"). Теперь попробуем реализовать, "чистый"
back-end, когда наше приложение будет сервисом данных (Resource service), а всю заботу об аутентификации пользователей 
возьмет на себя сервис KeyCloak. В качестве клиента задействуем PostMan.

Мы же помним, что у нас есть, как минимум 3-и части "головоломки" участвующих в цепочки OAuth2 аутентификации:
- **Resource Server** - хранилище ресурсов;
- **Authentication Server** - сервис определяющий кому и при каких условиях можно разрешать/запрещать доступ к resource service;
- **Client application** - посредник, между "Resource server" и "Authentication Server";

![OAuth2_Scheme](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/OAuth2_Scheme.jpg)

________________________________________________________________________________________________________________________
### Часть 5. - Применение KeyCloak в качестве OAuth2 сервиса авторизации для Resource service.

У нас уже есть установленный и настроенный KeyCloak (аж целых 3-и версии такового, [см. предыдущие версии приложения](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_11_2)), 
у нас есть приложение работающее как классическое "fullstack" web - приложение, где есть: блок хранения и работы с 
данными, блок отображения информации и блок безопасности.

Теперь нужно вырезать все лишнее и оставить только блок работающий с данными и модифицированную систему безопасности, 
у нас не будет "красивого" отображения данных, а всю нагрузку по аутентификации/авторизации "клиентского приложения" 
возьмет на себя KeyCloak. 

Клиентом (клиентским приложением) у нас будет выступать PostMan.

#### Этап 1. - Настроим KeyCloak сервис:

Фактически у нас все настроено, но нужно внести коррективы.
- Шаг 0. - заходим в KeyCloak под admin "учеткой" и переходим в наш созданный и настроенный realm;
- Шаг 1. - активируем "Direct access grants" в разделе Clients->Settings;
- Шаг 2. - отключаем весь "Consent screen", т.к. login форму мы использовать не будем;
- Шаг 3. - сохраняем настройки;

![KeyCloak_access_set](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/DOC/pic/1_KeyCloak_access_set.jpg)

#### Этап 2. - Настроим PostMan на запрос token-a к KeyCloak сервису:

У нас будет POST запрос:
- Шаг 1. - прописываем адрес запроса взятый из раздела "OpenID Endpoint Configuration" настройки realm-ов;
  
        http://localhost:9100/realms/SpringSecProject-OAuth-Test-Realm/protocol/openid-connect/token

- Шаг 2. - настраиваем параметры запроса в теле оного, как x-www-form-urlencoded;

        grant_type : password
        client_id : SpringSecProject-OAuth-Test-Client
        client_secret : 6xViFDG2RKbAS7drTXCtjhf3r0MbWA2v
        username : admin@test.com
        password : 1234

- Шаг 3. - отправляем запрос и получаем ответ от KeyCloak в виде JSON объекта;
- Шаг 4. - полученный access_token мы можем декодировать и изучить его структуру, но он нам нужен для работы;

![KeyCloak_from_PostMan_request](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/DOC/pic/2_KeyCloak_from_PostMan_request.jpg)

#### Этап 3. - Настроим наш сервер ресурсов (Resource service):

Как уже было упомянуто выше, мы возьмем один из прошлых проектов [Security_part_10_1](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_10_1) и вырежем из него все лишнее,
возможно что-то и оставим. Самое важное это подтянуть нужные зависимости, настроить цепочку фильтров безопасности и
добавить если нужно дополнительные bean-ы.

- Шаг 1. - добавляем зависимость:

        в build.gradle:    
        implementation "org.springframework.boot:spring-boot-starter-oauth2-resource-server:${versions.oauth2_rs}"

        в version.gradle:
        'oauth2_rs':'3.4.5'

- Шаг 2. - прописываем нужные настройки в [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/src/main/resources/application.yml):

        pring:
          security:
            oauth2:
              resourceserver:
                jwt:
                  issuer-uri: http://localhost:9100/realms/SpringSecProject-OAuth-Test-Realm
                  jwk-set-uri: http://localhost:9100/realms/SpringSecProject-OAuth-Test-Realm/protocol/openid-connect/certs

- Шаг 3. - добавляем нужный bean - [JwtDecoder](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/src/main/java/me/oldboy/config/jwt_pwe_config/JwtConfig.java#L26), в файл конфигурации безопасности нашего сервиса ресурсов - [JwtConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/src/main/java/me/oldboy/config/jwt_pwe_config/JwtConfig.java);
- Шаг 4. - запускаем сервис ресурсов - работает;

#### Этап 4. - Проверим отклик сервера ресурсов (Resource service) на GET запрос из PostMan:

Запрос приходит на сервис и в контекст безопасности попадает некий объект аутентификации, но он "непонятен" исходной логике 
приложения. Как бы мы не хотели удалить все лишнее из старой версии приложения многое нам понадобиться, а что-то придется 
переделать.

Например, у нас до сих пор есть записи о пользователях, т.к. на них завязана вся логика БД и это хорошо. Однако, из сервиса 
авторизации нам прилетает JWT token, который наш сервис "не понимает". Но, мы можем "распарсить" полученный token и извлечь 
например username (у нас это email). Далее мы можем применить его для доработки кода утилитного метода для контроллеров, 
т.к. email у нас является уникальным полем в БД и через него можно получить необходимые данные для ответа на запрос 
см. - [UserDetailsDetector.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/src/main/java/me/oldboy/controllers/util/UserDetailsDetector.java).

После коррекции кода нашего "детектора" и получения возможности извлекать нужные данные из самой БД, мы могли бы пойти 
путем использованном в [SecurityConfiguration.java](https://github.com/JcoderPaul/Spring_Framework_Lessons/blob/master/Spring_part_22/src/main/java/spring/oldboy/config/SecurityConfiguration.java) 
кастомного OAuth2UserService-а, т.е. извлекать все роли и привилегии из БД самого сервиса ресурсов. 

Но мы хотим попробовать извлекать роли прописанные в KeyCloak.

Пока, наш сервис ресурсов не знает откуда доставать роли пользователя и если мы обращаемся к endpoint-ам: "/myBalance", 
"/myContact" и т.д., то все нормально, данные из них доступны без определения ролей, а вот если бы мы захотели получить, 
например, доступ к "/admin/getAllClient", то получили бы отказ и "403 Forbidden" ответ от сервера. 

Решим этот вопрос!

Нужно сделать так, чтобы в token-e из KeyCloak прилетали роли зарегистрированного пользователя. Можно в самом KeyCloak 
посмотреть какие данные в какой token передаются, мы можем добавлять нужные и корректировать лишние. Для просмотра 
структуры token-ов нам нужно зайти в "Clients", далее выбрать нашего созданного клиента "SpringSecProject-OAuth-Test-Client",
и мы попадем в раздел "Client details". Тут мы выбираем закладку "Client scopes" и затем в незаметном подменю пункт "Evaluate":

![KeyCloak_client_details_evaluate](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/DOC/pic/3_KeyCloak_client_details_evaluate.jpg)

Выбираем пользователя чьи учетные данные передавали в теле запроса к KeyCloak см. выше, username : admin@test.com и 
смотрим раздел "Generated access token". Нас интересует "ключ" - "realm_access". При этом если пользователю не добавили 
ранее нужных ролей ROLE_ADMIN (ROLE_USER) и т.д. это нужно сделать сейчас (вспомним как это делать): заходим в раздел 
"Users", выбираем нужного пользователя, заходим в закладку "Role mapping" и через кнопку "Assign role" добавляем нужную
роль см.:

![KeyCloak_user_role_mapping](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/DOC/pic/4_KeyCloak_user_role_mapping.jpg)
        
Тогда в "realm-access" мы увидим добавленную ранее роль (ROLE_ADMIN):

![KeyCloak_client_details_evaluate_2](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/DOC/pic/5_KeyCloak_client_details_evaluate_2.jpg)

Теперь нужно "вытащить" эту роль в коде нашего ресурс сервера (ресурс сервиса) из полученного token-a. Для этого нам 
понадобиться [JwtAuthenticationConverter](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/src/main/java/me/oldboy/config/jwt_pwe_config/JwtConfig.java#L31), причем у него есть стандартная реализация - [OAuth 2.0 Resource Server JWT ](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
Мы реализуем свою в виде bean метода [*.jwtAuthenticationConverter()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/src/main/java/me/oldboy/config/jwt_pwe_config/JwtConfig.java#L31) в [JwtConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/src/main/java/me/oldboy/config/jwt_pwe_config/JwtConfig.java). И теперь, у нас есть доступ 
к endpoint-ам разрешенным к просмотру только пользователям с ROLE_ADMIN.

И так, все настроено и запущено, и имитировать работу цепочки взаимодействия "Клиент - Сервер ресурсов - Сервер авторизации" 
просто:
- Шаг 1. - при помощи PostMan обращаемся к сервису аутентификации KeyCloak (этап 2, см. выше) и получаем "access token";
- Шаг 2. - при помощи PostMan (выбираем Auth Type: Bearer Token, подставляем свежий полученный токен доступа) формируем 
GET запрос и обращаемся к любому доступному endpoint-у сервиса данных см. внутр. структуру контроллеров (в ответ 
"прилетают" JSON объекты);

        http://localhost:8080/admin/getAllClient

![PostMan_req_to_resource_service](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_3/DOC/pic/6_PostMan_req_to_resource_service.jpg)

На этом краткий обзор работы системы безопасности Spring framework-a, пожалуй, закончим.
________________________________________________________________________________________________________________________
### Key Class and Methods:

* [org.springframework.security.access.prepost](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/package-summary.html)

* [Class HttpSecurity](https://docs.spring.io/spring-security/site/docs/5.0.0.M5/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(javax.servlet.Filter,%20java.lang.Class))
* [Interface HttpSecurityBuilder](https://docs.spring.io/spring-security/site/docs/4.1.0.RC2/apidocs/org/springframework/security/config/annotation/web/HttpSecurityBuilder.html)

### Reference Documentation:

* [Class JwtAuthenticationConverter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/oauth2/server/resource/authentication/JwtAuthenticationConverter.html)

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

* [OAuth 2.0 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

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

* [Spring Security – Map Authorities from JWT](https://www.baeldung.com/spring-security-map-authorities-jwt)

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