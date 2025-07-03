### Настройка logout для Keycloak в Spring Boot приложении.

И так у нас приложение, которое использует сразу несколько способов аутентификации (пусть и однотипных). В нашей Spring 
Security цепи безопасности применяется OAuth2/OIDC, в частности KeyCloak. Отсюда процесс выхода их приложения (logout) 
включает несколько ключевых моментов. Нам важно не только завершить локальную сессию Spring, но и инициировать выход из 
Keycloak, чтобы обеспечить полноценный Single Sign-Out (SSO).

1. У нас есть все необходимые зависимости ([build.gradle](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/build.gradle)):
   
         implementation 'org.springframework.security:spring-security-oauth2-client'
         implementation 'org.springframework.boot:spring-boot-starter-security'
         implementation 'org.springframework.boot:spring-boot-starter-web'

2. У нас есть файл определяющий конфигурацию безопасности Spring Security - AppSecurityConfig.java. В нем необходимо
настроить обработчик выхода из системы (logout handler). Spring Security предлагает OidcClientInitiatedLogoutSuccessHandler 
для работы с OIDC. Его необходимо явно указать при настройке logout, например так:

         .logout(logout -> logout .logoutSuccessHandler(oidcLogoutSuccessHandler())
               .invalidateHttpSession(true)
               .clearAuthentication(true)
               .deleteCookies("JSESSIONID");

3. Сам обработчик Logout может выглядеть так:


         private LogoutSuccessHandler oidcLogoutSuccessHandler() { 
            OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler = 
               new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

            oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/login?logout");
         
            return oidcLogoutSuccessHandler; 
         }

Метод *.oidcLogoutSuccessHandler() создает OidcClientInitiatedLogoutSuccessHandler. Этот обработчик отвечает за отправку 
запроса на конечную точку выхода из Keycloak (OpenID Connect end_session_endpoint). В нем же мы указываем URI куда будет 
"переброшен" пользователь после выхода из приложения и его же необходимо указать в настройках самого KeyCloak - в разделе
"Valid Post Logout Redirect URIs"

4. Хотя наш файл [application.yml](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/src/main/resources/application.yml) содержит
все минимально необходимые настройки, повторимся и глянем как он должен выглядеть для KeyCloak (что-то можно пропустить):

         spring: 
           security: 
             oauth2: 
               client: 
                 registration: 
                   keycloak: # Имя нашего клиента (можно любое),опционально 
                     client-id: myapp-client-id # ID клиента, созданного в Keycloak 
                     client-secret: myapp-client-secret # Секрет клиента (если Access Type: confidential) 
                     authorization-grant-type: 
                     authorization_code redirect-uri: "{baseUrl}/login/oauth2/code/keycloak" 
                     scope: openid, profile, email # Необходимые скоупы 
                   provider: 
                     keycloak: # Имя провайдера (должно совпадать с именем клиента выше) 
                     issuer-uri: http://localhost:8080/realms/your-realm-name # URL нашего Keycloak Realm

5. Настройки клиента в Keycloak, критически важный шаг (хотя большую часть мы уже проделали ранее):
   - Открываем Keycloak Admin Console.
   - Переходим в наш Realm, у нас это - SpringSecProject-OAuth-Test-Client.
   - Выбираем пункт меню слева "Clients".
   - Находим нашего клиента, имя то же, что и ранее - SpringSecProject-OAuth-Test-Client.
   - На вкладке "Settings" в разделе "Access settings":
      - Root URL: Устанавливаем базовый URL нашего Spring приложения (у нас это - http://localhost:8080/).
      - Valid Redirect URIs: Добавляем URI, на которые Keycloak может перенаправлять после успешного входа (у нас это - http://localhost:8080/*).
      - **Valid Post Logout Redirect URIs: ОБЯЗАТЕЛЬНО** теперь добавляем URI, который указали в oidcLogoutSuccessHandler.setPostLogoutRedirectUri(). У нас это - http://localhost:8080/webui/bye
      - Web Origins: Если наше приложение запускается с другого домена или порта, конфигурируем его здесь (например, http://localhost:8080).

Конечно мы можем использовать Wildcard * (например, http://localhost:8080/*), но это менее безопасно, общий вид см.

![29_KeyCloak_logout_config](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/29_KeyCloak_logout_config.jpg)
 