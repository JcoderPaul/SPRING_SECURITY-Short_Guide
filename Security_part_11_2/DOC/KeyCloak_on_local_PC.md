### Вариант 1. - Установка и настройка KeyCloak на локальный компьютер (под Windows OS, development mode).
________________________________________________________________________________________________________________________
Ссылки:
- [https://www.keycloak.org/](https://www.keycloak.org/) ;
- Варианты загрузки - [https://www.keycloak.org/downloads](https://www.keycloak.org/downloads) ;
- [KeyCloak на GitHub](https://github.com/keycloak/keycloak) ;
- [OAuth Grant Types](https://oauth.net/2/grant-types/)
- [KeyCloak Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/index.html);
________________________________________________________________________________________________________________________
### Этап 1 - Установка KeyCloak на локальный компьютер.

- Шаг 1. - Находим через любую поисковую систему по запросу "KeyCloak" ссылку на сайт провайдера сервиса - [https://www.keycloak.org/](https://www.keycloak.org/).
- Шаг 2. - Выбираем вариант установки из дистрибутива (*.ZIP архива) - [https://www.keycloak.org/downloads](https://www.keycloak.org/downloads).
- Шаг 3. - Разворачиваем архив в любую удобную нам папку с говорящим именем.

Фактически процесс установки завершен, в полученном пакете можно изучить все содержимое в файле README.md кратко описано, что делать. 
________________________________________________________________________________________________________________________
### Этап 2 - Стартовая настройка KeyCloak.

Теперь займемся минимальной первоначальной настройкой (помним, мы в режиме разработки):

- Шаг 1. - Находим переменные среды: Система -> Дополнительные параметры системы -> Переменные среды -> Переменные среды пользователя для User (ваше имя) -> Path. Так же необходимо, что бы в переменных среды был настроен JAVA_HOME с указанием пути к рабочим JDK нашей машины;
- Шаг 2. - В переменную Path добавляем полный путь по папки "bin" развернутого KeyCloak сервера, например так: "C:\KeyCloakServer\keycloak-26.2.0\bin\" это позволит более удобно общаться с сервисом из командной строки;
- Шаг 3. - Первый запуск системы (см. ReadMe.md в основной папке KeyCloak), либо смотрим раздел - [https://www.keycloak.org/getting-started/getting-started-zip](https://www.keycloak.org/getting-started/getting-started-zip) с описанием действий, в зависимости от ОС запускаются разные скрипты у нас Windows, для запуска в командной строке применяем (внимание, это режим разработки не "боевой"): 

          kc.bat start-dev   

После первого запуска сервис KeyCloak обновит конфигурацию, может появиться, что-то вроде:

          Updating the configuration and installing your custom providers, if any. Please wait.
          [io.quarkus.deployment.QuarkusAugmentor] (main) Quarkus augmentation completed in 8839ms
          Running the server in development mode. DO NOT use this configuration in production.

По умолчанию сервис запускается на порту 8080, при необходимости (если занят) его можно сменить:

          INFO  [io.quarkus] (main) Keycloak 26.2.0 on JVM (powered by Quarkus 3.20.0) started in 14.629s. 
          Listening on: http://0.0.0.0:8080

Для управления настройками сервиса, и другими его полезностями доступен раздел - [https://www.keycloak.org/guides](https://www.keycloak.org/guides).
Нам нужен более узкий подраздел, связанный с конфигурированием - [All configuration](https://www.keycloak.org/server/all-config).

- Шаг 4. - Если порт 8080 у нас занят, сменим адрес/порт доступа к сервису авторизации, например на 9100, для этого используем при запуске настройку:

          kc.bat start-dev --http-port=9100

И мы видим условия запуска сервиса в этот раз уже на новом порту (адрес мы не меняли, он localhost):

          INFO  [io.quarkus] (main) Keycloak 26.2.0 on JVM (powered by Quarkus 3.20.0) started in 7.851s. 
          Listening on: http://0.0.0.0:9100

- Шаг 5. - Заходим в KeyCloak по адресу заданной настройки: http://localhost:9100/
- Шаг 6. - Задаем значения временного администратора (пароль/логин), пусть будет (admin/admin):

![KeyCloak_first_start](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/1_KeyCloak_first_start.jpg)

- Шаг 7. - Заходим в систему под созданной временной учетной записью администратора:

![KeyCloak_user_logIn_form](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/2_KeyCloak_user_logIn_form.jpg)

- Шаг 8. - При первом входе в систему, мы получим предупреждение, что стартовый аккаунт временный и его лучше заменить, т.е. создать новый с правами ROLE_ADMIN для "master realm".
- Шаг 9. - Делаем согласно пунктам меню:

![KeyCloak_make_new_admin](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/3_KeyCloak_make_new_admin.jpg)

Заходим в пользователя и корректируем необходимые пункты меню, а точнее: задаем пароль и роли:

![KeyCloak_new_admin_config](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/4_KeyCloak_new_admin_config.jpg)

Если с разделом "Credentials" все более менее понятно, то в разделе "Role mapping", не все так однозначно, и роль ROLE_ADMIN немного "спрятана" добавляем новую роль через "Assign role" и попадаем в меню выбора:

![KeyCloak_new_admin_role_assign](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/5_KeyCloak_new_admin_role_assign.jpg)

Выбираем "Filter by realm role", далее роль ADMIN и жмем "Assign". Новый админ создан. Так же можно создать и обычного пользователя данного сервиса.

На данном этапе у нас существует только "Master realm" - "Главное королевство", ну или "корневая область", для каждого нашего приложения мы должны создать
отдельную "область", "зону" или realm, чтобы однозначно идентифицировать всю связную с ней информацию и настройки безопасности, т.е. все последующие зоны 
(realms) будут порождаться от Master realm.
________________________________________________________________________________________________________________________
### Этап 3 - Создание своей зоны безопасности для нашего приложения - KeyCloak Realm.

Мы установили сервис аутентификации, запустили его и сделали первоначальные настройки для пользователя с правами ADMIN. 
Теперь нам нужно выделить отдельный раздел отвечающий за настройки безопасности и т.д. для нашего тестового приложения. 
Да, можно все делать в master realm, но даже сами разработчики KeyCloak рекомендуют под каждое приложение выделять отдельную 
зону настройки параметров безопасности и взаимодействия с клиентом. И так, создадим отдельную зону безопасности - realm - 
для нашего приложения SpringSecProject OAuth Test:

- Шаг 1. - Выбираем раздел "Manage realm" и кнопку "Create realm":

![KeyCloak_create_new_realm](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/6_KeyCloak_create_new_realm.jpg)

- Шаг 2. - Задаем имя будущего realm-a и жмем "Create":

![KeyCloak_enter_name_and_create](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/7_KeyCloak_enter_name_and_create.jpg)

Теперь наш новый realm в котором мы будем настраивать взаимодействие с нашим back-end приложением имеет вполне конкретный адрес: [http://localhost:9100/admin/master/console/#/SpringSecProject-OAuth-Test-Realm](http://localhost:9100/admin/master/console/#/SpringSecProject-OAuth-Test-Realm).
________________________________________________________________________________________________________________________
### Этап 4 - Создание клиента - KeyCloak client.

Теперь, в ранее созданном realm-e мы должны создать клиента, т.е. (обозначить) зарегистрировать наше приложение (ресурс сервер) 
на сервере аутентификации KeyCloak:

- Шаг 1. - Выбираем раздел "Clients", жмем кнопку "Create client" и попадаем в раздел описывающий по шагам создаваемого клиента:

![KeyCloak_create_client_1](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/8_KeyCloak_create_client_1.jpg)

- Шаг 2. - Задаем тип клиента, его ID, имя и описание при желании и жмем "Next":

![KeyCloak_create_client_2](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/8_KeyCloak_create_client_2.jpg)

- Шаг 3. - Задаем (оставляем по умолчанию) Standard flow (если смотреть текст под знаком вопроса, то мы увидим, что этот
тип flow включает стандартную аутентификацию на основе перенаправления OpenID Connect с кодом авторизации. В терминах 
спецификаций OIDC или OAuth2 мы используем поддержку «Authorization Code Flow» для данного клиента), и снова "Next":

![KeyCloak_create_client_3](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/8_KeyCloak_create_client_3.jpg)

- Шаг 4. - Прописываем Valid redirect URLs (их может быть несколько) и сохраняем данные "Save".
________________________________________________________________________________________________________________________
### Дополнительная настройка в разделе KeyCloak client (для Authorization Code Flow).

Для того чтобы использовать Authorization Code Flow нам понадобиться включить этот режим:

![KeyCloak_confidential_set](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/9_KeyCloak_confidential_set.jpg)

Этот переключатель определяет тип клиента OIDC. Когда он включен, тип — конфиденциальный доступ. Когда он выключен, 
тип — публичный доступ. В старой версии интерфейса KeyCloak это был переключатель Access Type. Из работы с Google и 
GitHub сервисами аутентификации мы помним, что при выбранном способе взаимодействия - Authorization Code Flow, нам 
нужны будут clientId и clientSecret, первый мы задали сами при создании клиента, но если забыли, то можно глянуть в 
Clients -> Settings:

![KeyCloak_klient_id](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/10_KeyCloak_klient_id.jpg)

Требуемый Client Secret можно найти в разделе Clients -> Credentials, в текущей версии программы от спрятан, но доступен 
для просмотра и копирования:

![KeyCloak_client_secret](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/11_KeyCloak_client_secret.jpg)

Именно с помощью них при запуске всей нашей цепи безопасности клиентское приложение Client получит "код авторизации" и затем 
"обменяет" его на токен доступа у сервера авторизации (впервые мы это опробовали на сервисе авторизации Google):

![Authorization_Code_Flow](https://github.com/JcoderPaul/Spring_Framework_Lessons/blob/master/Spring_part_22/DOC/AuthorizationCodeFlow.jpg)

________________________________________________________________________________________________________________________
### Этап 5 - Создание регулярного пользователя в KeyCloak (пока руками, не из клиентского приложения).

Мы помним, что для нормального взаимодействия всех частей цепи безопасности (пользователь - клиент - сервис авторизации - сервис данных), 
пользователь - "Resource owner", как минимум, должен быть зарегистрирован в сервисе аутентификации. Обычно данный процесс происходит именно
через клиентское приложение, т.е. обычный пользователь может и не иметь доступ к KeyCloak. Пока мы не используем приложение, а делаем все
через интерфейс сервиса авторизации (можно отметить, что email верифицирован):

![KeyCloak_create_simple_user](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/12_KeyCloak_create_simple_user.jpg)

Задаем пароль клиенту:

![KeyCloak_simple_user_credentials](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/13_KeyCloak_simple_user_credentials.jpg)

________________________________________________________________________________________________________________________
### Этап 6 - Тестирование настроек KeyCloak (с использованием PostMan-а и браузера).

Перед тем, как начать нам нужно понять с какими endpoint-ами в KeyCloak нам придется взаимодействовать. Для этого можно 
обратиться в раздел Realm settings, закладка General и в самом низу, над кнопкой Save есть два списка. Нам нужен -
OpenID Endpoint Configuration:

![KeyCloak_openID_endpoints](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/17_KeyCloak_openID_endpoints.jpg)

Тут в JSON формате мы найдем основное настройки нашего realm-a, а так же точки доступа применяемые при взаимодействии 
Client App и Auth service, соответственно нами при ручном тестировании процесса получения и обмена client authorization 
code на access token.

В идеале, нам бы сразу "прикрутить" развернутый на локальной машине KeyCloak сервис к уже существующему приложению и 
посмотреть, что будет. Но торопиться не будем - по задаче за раз! Посмотрим, как реагирует наш сервис аутентификации 
на запросы из PostMan-a и браузера. И так, сервис аутентификации запущен и настроен. Запускаем PostMan и формируем 
первый запрос. Именно формируем, т.к. в PostMan-е это делается более структурировано и наглядно. Этот запрос нужен, 
чтобы получить код авторизации - authorization code. 

- Шаг 1. - Получение кода авторизации. Мы должны обратиться к определенной точке доступа сервиса KeyCloak ("authorization_endpoint") 
методом GET (в адресе есть имя нашего realm-a - SpringSecProject-OAuth-Test-Realm и явно виден порт сервиса, что мы задали в начале, 
при настройке):

        http://localhost:9100/realms/SpringSecProject-OAuth-Test-Realm/protocol/openid-connect/auth

В Postman проведем предварительные настройки параметров (это те параметры, которые должен отправить Client или клиентское 
приложение к серверу авторизации):

![KeyCloak_get_req_auth_code_setting](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/14_KeyCloak_get_req_auth_code_setting.jpg)

И тогда окончательный вид запроса, который мы теперь будем делать в браузере выглядит, так:

        http://localhost:9100/realms/SpringSecProject-OAuth-Test-Realm/protocol/openid-connect/auth?response_type=code&client_id=SpringSecProject-OAuth-Test-Client&state=sdf3465b5ewrtyghh6u8e4&scope=openid profile&redirect_uri=http://localhost:8080/redirect

Какие параметры мы задали:
- response_type - какой тип ответа мы ожидаем от сервиса (сервера) авторизации (в данной ситуации мы говорим сервису, что ждем в ответ код авторизации, для дальнейшего его "обмена" на token доступа);
- client_id - данный параметр мы задали сами на шаге один четвертого этапа настройки KeyCloak, т.е. когда регистрировали нового клиента (очень похоже на то, что мы делали в сервисе Google, и клиент и пользователь должны быть в системе сервиса аутентификации);
- state - случайный набор символов, который должно генерировать клиентское приложение (этот же набор символов client app получит в ответ от AOuth сервера в момент возврата кода авторизации);
- scope - определяет какие права пользователь получает от сервера авторизации, для взаимодействия c сервисом ресурсов;
- redirect_url - адрес куда сервис авторизации направит ответ на текущий запрос с ранее полученным state (чтобы клиентское приложение точно знало, что это ответ на его запрос) и сгенерированный код авторизации;

И так мы делам запрос не в PostMan, а в браузере и попадаем на страницу авторизации KeyCloak:

![KeyCloak_brouser_resp_form](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/15_KeyCloak_brouser_resp_form.jpg)

Вводим данные регулярного пользователя, что создали на пятом этапе и авторизуемся. KeyCloak перенаправит нас на страницу redirect-a,
но поскольку ее нет, и никто обработать ответ пока не может, то мы получим некий стандартный ответ в браузере, о невозможности отобразить 
данные или об отказе в доступе. Для нас в этой ситуации самым важным является строка ответа (разделим ее для удобства чтения):

        http://localhost:8080/redirect?
        state=sdf3465b5ewrtyghh6u8e4
        &
        session_state=e235e3b2-4b60-4672-91e1-0dfa0d403d2c
        &
        iss=http%3A%2F%2Flocalhost%3A9100%2Frealms%2FSpringSecProject-OAuth-Test-Realm
        &
        code=e5b2f91b-1ccd-4943-93cd-e1da55106fd5.e235e3b2-4b60-4672-91e1-0dfa0d403d2c.d208a6e7-d6ee-463b-8221-1d2751c6fd36

Мы видим наш state, что был отправлен в запросе, из чего можем (клиентское приложение может) сделать вывод - это ответ
на наш запрос. И самое главное, мы видим код авторизации - code. Его Client app на следующем шаге должно будет "обменять" 
на access token к серверу ресурсов.

Обычно весь процесс "получения и обмена" одних "ключей" на другие происходит между "Client app" и "Auth сервисом" довольно 
быстро. По умолчанию данный диапазон равен 1 min, и в KeyCloak его можно настраивать - Client Login Timeout, см:

![KeyCloak_client_login_timeout](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/16_KeyCloak_client_login_timeout.jpg)

Эту особенность мы упомянули для того, чтобы в процессе текущего и дальнейшего тестирования избежать "непоняток" с 
невозможностью провести обмен "кода клиента" на "ключи доступа" из-за короткого срока жизни первого. Когда все отлажено 
и работает всему комплексу программ одной минуты будет достаточно для завершения авторизации и организации доступа к 
сервису ресурсов.

А пока немного раздвинем границы времени!

- Шаг 2. - Получение токенов доступа - Access token.

Тут нам понадобится другая конечная точка доступа к KeyCloak ("token_endpoint") и к ней мы уже будем обращаться методом POST:

       http://localhost:9100/realms/SpringSecProject-OAuth-Test-Realm/protocol/openid-connect/token

В нашем PostMan-e подготовим все к запросу, т.к. все предварительные параметры запроса нам известны, кроме "кода аутентификации",
который мы должны "живенько" получить на первом шаге этого раздела и подставить в соответствующий параметр данного запроса:

![KeyCloak_get_access_token](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/18_KeyCloak_get_access_token.jpg)

Особенность этого запроса в том, что все данные передаются сервису аутентификации в теле запроса:
- grant_type - если грубо то это "протокол безопасности" или взаимодействия всех элементов системы (client app - OAuth service - resource service) см. [OAuth Grant Types](https://oauth.net/2/grant-types/);
- client_id - как и на первом шаге данного раздела - это ID клиентского приложения;
- client_secret - мы получили при создании нового Client и можем найти перейдя в Clients -> Credentials;
- code - сюда подставляем код полученный на первом шаге;
- redirect_uri - пока подставляем сюда заданный нами адрес при первичных настройках (тот же, что и на первом шаге, пока это "тест заглушка");

После отправки запроса сервису мы получим ответ в виде JSON объекта первым ключом которого будет ожидаемый нами "access_token".
Именно с ним "Client app" обратится к "Resource service":

![Authorization_Code_Flow](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/Authorization_Code_Flow.jpg)

И так, мы настроили наш сервер аутентификации KeyCloak для работы с Authorization code flow (по такому же принципу мы можем настроить и протестировать другие "поточные схемы безопасности").
________________________________________________________________________________________________________________________
### Дополнительная настройки KeyCloak (Scope and Consent).

При настройке Google мы помним, что возникли некие задержки, сервис предложил нам настроить Consent и только затем мы 
смогли продолжить регистрацию нашего Client app и настройку scope. Тут мы тоже можем регулировать, какие допуски имеет
пользователь на сервере данных (ресурс сервере) и какие данные пользователя (resource owner) доступны в передаваемых 
token-ах.

Нужно помнить, что OIDC это надстройка над OAuth2, она позволяет авторизовать пользователя. См. выше рис., где отображается
набор возвращаемых KeyCloak token-ов: "access_token" и "id_token". Токены эти легко декодируются ([https://jwt.io/](https://jwt.io/)) 
и мы можем посмотреть их содержимое. Считается, что в id_token-e, мы можем и должны передавать права (роли) пользователя и 
другую необходимую информацию о нем (claims), хотя весь этот необходимый набор данных мы можем передавать и в access-token-e.

### Scope and claims
 
Именно поле Scope, в первичном запросе от клиентского приложения, определяет что будет (не будет) формироваться и передаваться 
сервисом авторизации в ответ. Если мы оставим только Scope openid, то в ответ мы гарантированно получим оба и "access_token" и 
"id_token" со стандартным набором данных, который мы можем подкорректировать и добавить или убрать нужные нам в настройках 
KeyCloak (если поле scope запроса не содержит openid, то и id_token в ответ мы не получим). Структуру передаваемых claim-ов,
на сервере авторизации мы тоже можем регулировать.

Т.е. еще раз, поле scope запроса к сервису авторизации определяет какие данные будут получены в ответ и будут доступны для 
работы с ними, например, в клиентском приложении (email, username и т.д.).

В разделе "Client scopes" основного меню KeyCloak мы можем найти стандартный расширяемый набор готовых scopes:

![KeyCloak_client_scopes](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/19_KeyCloak_client_scopes.jpg)

Настройки scope-ов уже готовых или вновь созданных мы тоже можем регулировать (см. profile scope):

![KeyCloak_scope_profile_set](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/20_KeyCloak_scope_profile_set.jpg)

Мы даже можем задавать, какие данные будут передаваться с выбранным scope (см. profile scope):

![KeyCloak_scope_mappers](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/21_KeyCloak_scope_mappers.jpg)

И будут ли они добавляться в token-ы в виде claim-ов, например (profile scope username mapper detail):

![KeyCloak_mapper_details](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/22_KeyCloak_mapper_details.jpg)

И так, KeyCloak предоставляет нам достаточно широкий выбор настроек "области доступа" - scope.  

### Consent screen

Как и в случае с Google мы можем настроить KeyCloak для отображения "Consent screen". Если вернуться к первому шагу 
шестого этапа, то мы увидим, что после отправки запроса, в ответ получаем меню аутентификации. Далее мы вводили пароль и
логин и получали в ответ код авторизации, без предупреждений. Если вспомнить аутентификацию на Google, то мы перед тем 
как вводить учетные данные получали некое предупреждение о том, что предоставляем стороннему приложению доступ к 
определенному набору наших данных (email, username и т.п.) и даже права на совершения неких действий на сервисе ресурсов 
(edit, delete и т.д.).

Настроем "Consent screen" предупреждения в KeyCloak ("усилим защиту") в меню Clients -> выбираем наше приложение SpringSecProject-OAuth-Test-Client -> Settings:

![KeyCloak_consent_screen_set](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/23_KeyCloak_consent_screen_set.jpg)

Используя данные настройки, мы можем активировать предупреждение, в ходе аутентификации на сервере авторизации, для пользователя: 

![KeyCloak_consent_screen_view](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/24_KeyCloak_consent_screen_view.jpg)

После получения такого "предупреждения" пользователь видит, что клиентское приложение - SpringSecProject-OAuth-Test-Client - 
пытается получить "доступ до..." и на данном дополнительном шаге может подтвердить или отказаться от своих действий.

Т.е. мы можем регулировать данную настройку и дополнительно предупредить пользователя.
________________________________________________________________________________________________________________________
### Этап 7 - Добавление ролей в KeyCloak.

Есть вариант при котором сервис KeyCloak будет использоваться исключительно как "сервис авторизации", т.е. с минимальными
настойками, исключительно для доступа к серверу ресурсов. Далее уже на самом сервисе ресурсов будет реализовано разграничение
по ролям и правам для каждого пользователя, такое возможно, да. Все наши предыдущие примеры были реализованы именно как 
классическое web-приложение "все в одном". Но сам KeyCloak позволяет задавать роли для зарегистрированных в нем пользователей.

Сделаем это для двух вариантов наших пользователей, зададим: ROLE_ADMIN и ROLE_USER:

- Шаг 1. - Создадим роли ADMIN и USER (это роли для текущего realm-a, т.е. для клиентского приложения):

![KeyCloak_create_roles](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/25_KeyCloak_create_roles.jpg)

- Шаг 2. - Зададим созданную роль выбранному клиенту, в нашем сервисе ресурсов пользователь "user3@test.com" имеет роль "USER":

![KeyCloak_assigne_role](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_11_2/DOC/pic/26_KeyCloak_assigne_role.jpg)

И так роли заданы, теперь дело за малым - настроить наш сервис ресурсов, что бы он работал с нашим настроенным KeyCloak-ом. 

### И самое главное, делаем экспорт всех доступных к выгрузке данных в папку keycloak, чтобы перенести их в KeyCloak запущенный в Docker контейнере.
________________________________________________________________________________________________________________________
Фактически данных настроек KeyCloak достаточно, как сервиса авторизации, для работы с нашим приложением в качестве сервера 
ресурсов (и Client app). Можно тестировать работу.