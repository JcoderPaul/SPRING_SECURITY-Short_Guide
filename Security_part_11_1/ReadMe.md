### Simple Spring App with Security (Part 11_1) - применение OAuth2 в защите приложений (начало).

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle
________________________________________________________________________________________________________________________
Повторение прошлого, в представленных материалах достаточно подробно рассмотрен текущий вопрос:
- [Spring Security Starter и OAuth-2.0 (Теория)](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_22#oauth-20-%D1%82%D0%B5%D0%BE%D1%80%D0%B8%D1%8F);
- [Технология OAuth 2.0](https://github.com/JcoderPaul/Spring_Framework_Lessons/blob/master/Spring_part_22/DOC/OAuth_2_0/AuthorizationWithOAuth_2_0.txt);
________________________________________________________________________________________________________________________
### Часть 1. - OAuth2 (Google as Authorization service).

У нас есть рабочее приложение с web интерфейсом и самописной страницей авторизации, теперь мы хотим сделать так, чтобы 
пользователь мог аутентифицироваться в нашем сервисе (приложении) при помощи стороннего сервиса авторизации (Google, 
GitHub и т.д.). Обратимся к ранее изученным материалам ["OAuth-2.0 (Теория)"](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_22#oauth-20-%D1%82%D0%B5%D0%BE%D1%80%D0%B8%D1%8F) и 
["Настройка OAuth-2.0 в web-приложении (Google as Authorization service)"](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_22#lesson-113---%D0%BD%D0%B0%D1%81%D1%82%D1%80%D0%BE%D0%B9%D0%BA%D0%B0-oauth-20-%D0%B2-web-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B8-google-as-authorization-service).

Добавляем в наше приложение важные зависимости:

      /* Набор классов для взаимодействия приложения и сервиса авторизации */
      implementation 'org.springframework.security:spring-security-oauth2-client'
  
      /* Набор классов для работы токенами, в частности JWT */
      implementation 'org.springframework.security:spring-security-oauth2-jose'

Учетная запись о нас в Google есть, в нашем приложении данные о нас тоже есть, дело за малым: исходя из изложенного мы 
должны зарегистрировать наше приложение (наш сервис) в Google, делаем:

- Шаг 0. - Заходим в наш аккаунт Google (аутентифицируемся).
- Шаг 1. - Переходим на ["Credentials page"](https://console.cloud.google.com/apis/credentials).
- Шаг 3. - Из раздела "Select a project":

![Select_a_project](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_google/1_Select_a_project.jpg)

- Шаг 4. - Создаем новый проект:

![New_project](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_google/2_New_project.jpg)

- Шаг 5. - Выбираем созданный проект (можно и через "Select a project"):

![Select_created_project](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_google/3_Select_created_project.jpg)

- Шаг 6. - Создаем параметры входа (Credentials) для нашего приложения (мы же помним, что теперь оно клиент):

![Auth_client_ID](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_google/4_Auth_client_ID.jpg)

- Шаг 7. - Однако на данном шаге, нам придется настроить "разрешения". Конфигурируем разрешения (ограничения) для нашего приложения (сервиса):

![Configure_consent_screen](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_google/5_Configure_consent_screen.jpg)

- Шаг 8. - Вводим данные приложения: название приложения и контактную инф., выбираем External - Доступно любому тестовому 
пользователю с учетной записью Google и еще раз вводим свой e-mail (или контактный email), соглашаемся с политикой Google API и 
создаем "разрешения" (credentials) для нашего приложения. 

Теперь мы настроили "разрешения" нашего приложения. Возвращаемся на ["Credentials page"](https://console.cloud.google.com/apis/credentials) и 
повторяем шаг 6. На этот раз мы можем создать "Create OAuth client ID":

- Шаг 9. - Выбираем Web-application:

![Create_OAuth_client_ID_step_1](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_google/6_Create_OAuth_client_ID_step_1.jpg)

- Шаг 10. - Именно тут, в разделе "Create OAuth client ID", мы должны ввести redirect_URI: http://localhost:8080/login/oauth2/code/google. 
Этот адрес можно найти в документации по [Spring Security](https://docs.spring.io/spring-security/reference/index.html), а именно [Core Configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html) или еще точнее ["Setting the Redirect URI"](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html#oauth2login-sample-redirect-uri). Конечно мы его можем изменить сообразно настройкам 
нашего приложения, именно на этот адрес будет возвращаться код авторизации - Authorization Code. Тут же можно добавить и 
http://localhost:8080/swagger-ui/oauth2-redirect.html, если в проекте есть Swagger и мы хотим подключить OAuth2.0 к нему. 
Жмем "CREATE".
- Шаг 11. - Получаем Client ID и Client secret (аналоги логина и пароля для пользователя) для нашего приложения. Именно с 
ними наше приложение будет обращаться к сервису авторизации (Google), когда пользователь выберет аутентификацию с помощью 
Google.
________________________________________________________________________________________________________________________
### Часть 2. - Согласование данных из OAuth2.0 сервиса с нашим web-приложением.

Теперь нужно настроить Spring Security нашего приложение для работы с Google OAuth.

- Шаг 1. - Настраиваем файла свойств application.yml:

      security:
        oauth2:
          client:
            registration:
              google:
                clientId: ... Client ID полученный от Google ...
                clientSecret: ... Client secret полученный от Google ...
                redirectUri: http://localhost:8080/login/oauth2/code/google
                scope: openid,email,profile

- Шаг 2. - Настраиваем наш файл конфигураций безопасности AppSecurityConfig.java:
  
      .oauth2Login(oauthConfig -> oauthConfig.loginPage("/webui/login")
										     .defaultSuccessUrl("/webui/main"));

- Шаг 3. - Настраиваем нашу Login форму (login.html и login_form.css), так, чтобы совмещать два вида входа в наше приложение:

      <div class="reg_button_zone">
          <a href="/oauth2/authorization/google" class="button">Login with Google</a></td></tr>
      </div>

- Шаг 4. - Окончательно корректируем файл конфигурации безопасности, мы хотим иметь возможность входить в сервис старым 
способом (через форму вода пароля и логина) и по средствам OAuth2, значит в security chain должны быть эти настройки 
(и фильтры):

      .formLogin(login -> login.loginPage("/webui/login")
                               .defaultSuccessUrl("/webui/main"))
      .oauth2Login(oauthConfig -> oauthConfig.loginPage("/webui/login")
                                             .defaultSuccessUrl("/webui/main")
                                             .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService())));

- Шаг 5. - Настраиваем взаимодействие нашего приложения (сервиса) с полученными из сервиса аутентификации учетными данными - 
настраиваем OAuth2LoginConfigurer. Очень важный момент в том, что возвращая сервисом OAuth информация может не совпадать с
той, которую "понимает" наше приложение. Скорее всего полученные данные будут близки по содержанию к применяемым в нашем 
приложении, но их все равно нужно адаптировать, этим у нас будет заниматься метод - *.oidcUserService() класса AppSecurityConfig.

Несложно увидеть, если включить режим DEBUG в SecurityConfig, какой набор фильтров при текущей настройке filter chain у нас 
используется:

        Security filter chain: [
              DisableEncodeUrlFilter
              WebAsyncManagerIntegrationFilter
              SecurityContextHolderFilter
              HeaderWriterFilter
              CorsFilter
              LogoutFilter
                OAuth2AuthorizationRequestRedirectFilter
                OAuth2LoginAuthenticationFilter
                UsernamePasswordAuthenticationFilter
              RequestCacheAwareFilter
              SecurityContextHolderAwareRequestFilter
              RememberMeAuthenticationFilter
              AnonymousAuthenticationFilter
              ExceptionTranslationFilter
              AuthorizationFilter
        ]

________________________________________________________________________________________________________________________
### Часть 3. - OAuth2 (GitHub as Authorization service).

Фактически у нас уже все было сделано в первой части, мы даже подключили Google в качестве сервера авторизации. Теперь 
сделаем то же самое, но уже с GitHub. Т.е. в нашей форме будет две кнопки для использования сторонних сервисов 
авторизации и конечно сохраниться стандартный вариант входа в приложение.

- Шаг 0. - Заходим в наш аккаунт GitHub (аутентифицируемся).
- Шаг 1. - Переходим в пункт меню Settings (настройки):

![GitHub_Settings](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_github/1_GitHub_Settings.jpg)

- Шаг 2. - В основном меню (слева) находим и выбираем пункт меню Developer settings (настройки разработчика):

![GitHub_Developer_Settings](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_github/2_GitHub_Developer_Settings.jpg)

- Шаг 3. - В появившемся меню выбираем пункт "OAuth Apps":

![GitHub_OAuth_Apps](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_github/3_GitHub_OAuth_Apps.jpg)

- Шаг 4. - Поскольку вы читаете этот раздел, то вероятно, вы проходите эти шаги впервые, поэтому у вас скорее всего 
нет приложений использующих GitHub в качестве сервера авторизации, а значит искомая кнопка "New OAuth app" будет в 
центре экрана. Если же вы уже делали нечто подобное, но забыли как это было, то кнопка может находиться вверху справа:

![GitHub_New_OAuth_app](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_github/4_GitHub_New_OAuth_app.jpg)

- Шаг 5. - Заполняем форму "Register a new OAuth app" и жмем кнопку "Register application" (см. внимательно callback URL):

![GitHub_Register_new_OAuth_app](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_github/5_GitHub_Register_new_OAuth_app.jpg)

- Шаг 6. - Получаем "ClientID" и генерируем "Client secrets" (они нам, как и ранее понадобятся при настройке application.yml):

![GitHub_Get_OAuth_app_ClientId_and_ClientSecret](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_1/DOC/pic/with_github/6_GitHub_Get_OAuth_app_ClientId_and_ClientSecret.jpg)

- Шаг 7. - Прописываем необходимые данные в application.yml (поскольку мы используем 2-а сервера авторизации их мы и указываем):

          security:
            oauth2:
              client:
                registration:
                  github:
                    clientId: ... app GitHub client ID ...
                    clientSecret: ... app GitHub client secret ...
                    redirectUri: http://localhost:8080/login/oauth2/code/github
                  google:
                    clientId: ... app Google client ID ...
                    clientSecret: ... app Google client secret ...
                    redirectUri: http://localhost:8080/login/oauth2/code/google
                    scope: openid,email,profile

- Шаг 8. - Добавляем кнопку аутентификации через GitHub в login форму (аналогично Google кнопке):

        <div class="reg_button_zone">
            <a href="/oauth2/authorization/github" class="button">Login with GitHub</a></td></tr>
        </div>

- Шаг 9. - Проблема несовпадения учетных данных возвращаемых с GitHub и тех, что используется нашим приложением (сервисом)
всплыла вновь, но на этот раз на нужно настроить наш кастомный OAuth2UserService, у нас уже есть один *.userInfoEndpoint(),
добавляем еще один и передаем в него наш CustomOAuth2UserService.java:

        .oauth2Login(oauthConfig -> oauthConfig.loginPage("/webui/login")
                                               .defaultSuccessUrl("/webui/main")
                                               .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
                                               .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))

- Шаг 10. - Настроим CustomOAuth2UserService.java так, что бы он возвращал понятные аутентификационные данные с нашими 
"ролями" и "разрешениями", а не теми, что возвращает сервер авторизации (т.е. адаптируем их) см. реализацию класса сервиса.

Настройка закончена, запускаем приложение и проверяем работу всех типов (и сервисов) аутентификации.

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