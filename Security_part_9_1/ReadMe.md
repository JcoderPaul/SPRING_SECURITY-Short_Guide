### Simple Spring Boot App with Security (Part 9_1) - применение JWT токена (начало).

- Spring Boot 3.3.8
- Spring Security 6.3.6
- Java 17
- Gradle
________________________________________________________________________________________________________________________
### Часть 1. - JWT Token (теория).

Ранее мы уже столкнулись с несколькими вариантами token-ов, очень близок по духу к JWT токену, RememberMe токен. Чуть 
отличается технология использования, но последовательность шагов при обращении к защищенному сервису сохраняется:
- клиент обращается к сервису - вводит логин и пароль;
- сервис проверяет введенные данные, и если они верны - высылает в ответе JWT-токен (т.е. в response);
- клиент при каждом последующем запросе к сервису высылает JWT-токен в заголовке Authorization (т.е. в request);
- сервис проверяет присланный клиентом JWT-токен, при каждом запросе (обращении к сервису), на подлинность;
- если JWT токен подлинный (не просроченный) - сервис высылает соответствующий ответ на полученный запрос;

Основная хитрость тут в том, что для работы с JWT токеном, как и для RememberMe токена, в цепи фильтров безопасности мы 
задаем SessionCreationPolicy.STATELESS, т.е. в данной ситуации Spring Security никогда не создаст Http Session и никогда 
не будет использовать ее для получения SecurityContext. Тут намек на то, что мы не сможем передать состояние контекста 
безопасности в JSESSIONID например. См. алгоритм выше, см. картинку ниже:

![jwt-workflow.png](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/DOC/pic/jwt-workflow.jpg)
________________________________________________________________________________________________________________________
### JWT токен vs Simple Hash-Based Remember-Me токен:

Мы уже упомянули, что JWT-токен (JSON Web Token) похож на Simple Hash-Based Remember-Me токен, рассмотренный в раннем
разделе. Их схожесть в том, что:
- они оба выдаются клиенту после успешного ввода имени и пароля (т.е. аутентификации);
- клиент отправляет token серверу при каждом запросе в заголовке (либо в cookies);
- сервер проверяет подлинность токена путем простой калькуляции hash-a, не проводя полную дешифрацию, и удостоверяется, что клиент именно тот, за кого себя выдает;
- JWT-токен имеет срок жизни и продолжает работать даже после того, как одна из сторон разорвала связь, например сервер был перезапущен;
- JWT-токен работает, если серверов несколько (с балансировщиком нагрузки), поскольку на конкретный сервер ничего не завязано: сессий нет, данных, хранящихся в сессии нет.

![how-jwt-work.png](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/DOC/pic/how-jwt-work.jpg)

Их ключевые отличия:

- В JWT токене можно хранить больше данных, чем в Simple Hash-Based Remember-Me токене. 
- В JWT-токене есть специальная часть Payload - полезная нагрузка, туда можно записать, например, роли и разрешения (допуски),
что позволяет при определенной логике взаимодействия клиент-сервера не обращаться к БД с запросами оных каждый раз, а просто 
достать их из токена. 
- Ничего секретного в Payload записывать нельзя. Обычно достаточно имени пользователя.
- При использовании JWT-токена в серьезных приложениях со Spring Security, потребуется написать больше кода, чем с Remember-Me.
- Для вычисления подписи в JWT-токене не используется пароль пользователя.
- Поскольку сессий нет, статус взаимодействия клиента и сервера хранится на клиенте, а не на сервере (на сервере состояние 
хранить негде).
________________________________________________________________________________________________________________________
### Структура JWT:

Как показано на [официальном сайте разработчиков JWT](https://jwt.io/), он состоит из трех частей, разделенных точкой:
- заголовок (Header);
- полезная нагрузка (Payload);
- подпись (JWT Signature Verification);

Заголовок и полезная нагрузка фактически передаются в открытом виде, хоть и выглядят как непонятные символы. Это формат 
base64, и если вставить их в [base64 онлайн-декодер](https://www.base64decode.org/), то получим исходник.

А вот третья часть - hash, или подпись, и есть особенность JWT token-a, гарантирующей его подлинность. Из нее нельзя 
получить «исходник». Что в данном случае и необязательно. "Исходники лежат" на сервере (в сервисе) и из них подпись можно получить 
сколь угодно много раз. Что сервис и делает - именно так проверяется подлинность токена.

    Составные части полезной нагрузки называются claims. Они могут быть registered (состоящими из 3 букв, например 
    iat — «issued at»), public и private. Мы можем загрузить в payload столько claims, сколько захотим. Существует 
    список стандартных claims для JWT payload — вот некоторые из них:
    
    - iss (issuer) — определяет приложение, из которого отправляется токен;
    - sub (subject) — определяет тему токена;
    - exp (expiration time) — время жизни токена;
________________________________________________________________________________________________________________________
### Как проверяется подлинность токена?

Берется заголовок, полезная нагрузка и секретный ключ, из них вычисляется некоторое значение - подпись.

Секретный ключ хранится на стороне сервера. С помощью него подпись вычисляется при первоначальной выдаче токена, и с 
помощью него же перевычисляется каждый раз, когда приходит токен (иначе говоря, токен проверяется на валидность). Если 
значение вычисленной подписи совпадает с тем, что в токене, то токен считается валидным.  

Это принцип поверки, аналогичный тому, что используется в Simple Hash-Based Remember-Me токене. Но, тут сервису (серверу), 
для проверки JWT не надо даже обращаться к БД и находить пароль пользователя, чтобы вычислить подпись. Пароль в расчет 
не входит, формула такая:

    HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload) + "." + ”mysecretkey”)

Важно помнить, что JWT-токен на сервере не хранится, а просто каждый раз проверяется на подлинность с помощью вышеприведенной 
формулы. Хранится только секретный ключ — он един для всех JWT-токенов, выпускаемых сервисом (сервером, приложением). У нас он
находится в файле SecurityConstants.java.

### В каком заголовке передается JWT-токен?

Обычно JWT токен передается в заголовке (response/request), откуда и читается/записывается:

    Authorization: Bearer <...generated token...>

________________________________________________________________________________________________________________________
### Часть 2. - Пример приложения с JWT-токеном (девиантный подход).

Необходимая предварительная настройка - загрузка требуемых зависимостей:

        runtimeOnly "io.jsonwebtoken:jjwt-impl:0.12.6"
        implementation "io.jsonwebtoken:jjwt-api:0.12.6"
        runtimeOnly "io.jsonwebtoken:jjwt-jackson:0.12.6"

Мы реализуем два варианта приложения: 
- Первое с неким минимальным front-end интерфейсом, причем все будет "в одном флаконе", т.е. сервис будет генерировать 
токен и сам же будет его проверять, а самое "крамольное", что и back-end и front-end будут в монолитной связке. 
- Второй вариант приложения будет неким REST API с использованием JWT-токена, как и в теории сервис будет генерировать 
JWT токен и отдавать его клиенту в ответ, на этом его функции заканчиваются, ну разве что еще проверка подлинности при 
запросе от клиента на защищенный endpoint.

И так, тут мы рассматриваем первый вариант реализации приложения (реализуем обязательные части):
- напишем JWT генератор (формирующий токен) - [JwtTokenGeneratorAndAfterFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/JwtTokenGeneratorAndAfterFilter.java);
- напишем JWT validator (проверяющий токен на подлинность, своеобразный фильтр авторизации) - [JwtTokenValidatorAndBeforeFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/JwtTokenValidatorAndBeforeFilter.java);
- добавим файл JWT констант, наш секретный ключ и название заголовка для передачи токена;

Поскольку мы уже упомянули о "крамольности" данного проекта, внесем разъяснения. В теоретическом разделе, да и много ранее, 
было упомянуто, что существует некая предпочтительная схема разделения любого проекта, на клиентскую часть (front-end приложение,
android приложение) и серверную части (back-end сервис, REST-сервис). Как раз в такой схеме очень легко и элегантно можно
реализовать взаимодействие этих двух частей при помощи JWT токена. А самое главное, пример реализации будет выглядеть логично 
и понятно - все согласно выше описанному алгоритму запросов и ответов. 

Если же мы реализуем "монолит", то применение JWT токена выглядит весьма сомнительно, можно сказать бессмысленно, хотя,
возможно, все будет зависеть от конкретной реализации. И так, в связи с этим, нам понадобится еще пара деталей к обязательным 
частям проекта:
- разработаем несложный web интерфейс для взаимодействия клиент-сервис (основные страницы - templates, файлы стилей - css);
- фильтр перехватывающий (response) ответ от сервера (сервиса) в виде JWT токена и перенаправляющий его в (request) тому 
же самому серверу (сервису) при запросе требуемой страницы (я же предупреждал, что будет слегка девиантно) - [SetRequestHeaderFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/SetRequestHeaderFilter.java);
- создадим класс, который будет хранить сгенерированный сервисом JWT токен, опять же на стороне сервера (безумие продолжается);
- расширим полномочия нашего слушателя аутентификации [AuthenticationEventListener.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/config/auth_event_listener/AuthenticationEventListener.java), добавим в него возможность запоминать 
данные аутентифицированного пользователя для последующей передачи их в генератор JWT токена;

Для чего вдруг понадобились такие хитрости в последних двух пунктах, можно сказать излишества. 

Мы же помним, что у нас нет сессии созданной системой безопасности Spring и данные пользователя негде хранить при последующем 
вызове, т.е. после прохождения формы аутентификации мы получаем подтверждение подлинности, но когда переходим на следующую 
страницу, например, *.defaultSuccessUrl(), мы снова проходим всю цепь фильтров безопасности и значит теряем данные 
аутентифицированного пользователя полученного ранее. Поэтому мы сохраняем результаты аутентификации и подставляем их в 
генератор JWT токена - [JwtTokenGeneratorAndAfterFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/JwtTokenGeneratorAndAfterFilter.java), каждый раз при запуске этого фильтра. 

Но это пол беды! Если вспомнить, какова логика применения JWT токена, то встает вопрос, а кто же в монолите, где нет явно 
выделенной front-end части, будет хранить и передавать сгенерированный сервисом токен - сам же сервис и будет, т.е. сам 
сгенерировал, сам и запомнил, сам выдал, сам же и получил. При этом все шаги, что мы описали в первой части остаются и 
работают.

Сразу возникает вопрос, зачем столько нелогичных сложностей, почему не выделить front-end в отдельное приложение или на 
худой конец использовать уже готовые решения. Все это будет, но в следующей части. А тут ответ простой - мы хотели посмотреть,
а что если? И так, поехали!

Основные блоки приложения (сервиса):
- back-end раздел (me.oldboy):
  - [config](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/config) - конфигурационный файлы Spring Security:
    - [AuthenticationEventListener.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/config/auth_event_listener/AuthenticationEventListener.java) - слушатель процедуры аутентификации;
    - [CustomLogoutHandler.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/config/logout_handler/CustomLogoutHandler.java) - обработчик процедуры "разлогинивания" - выхода из сервиса;
    - [securiry_details](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/config/securiry_details) - наши кастомные UserDetails и UserDetailsService (см. ранние разделы);
    - [AppSecurityConfig.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/config/AppSecurityConfig.java) - основной файл настройки цепи фильтров безопасности;
  - [SecurityConstants.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/constants/SecurityConstants.java) - файл констант безопасности (секретный пароль и название заголовка для хранения JWT);
  - [controllers](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/controllers) - раздел контроллеров:
    - [api](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/controllers/api) - REST контроллеры; 
    - [webui](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui) - контроллеры взаимодействия с web интерфейсом;
  - [dto](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/dto) - "межслойные" передатчики данных;
  - [exception](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/exception) - кастомные исключения;
  - [filters](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/filters) - раздел фильтров:
    - [JwtTokenGeneratorAndAfterFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/JwtTokenGeneratorAndAfterFilter.java) - фильтр генератор JWT токена;
    - [JwtTokenValidatorAndBeforeFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/JwtTokenValidatorAndBeforeFilter.java) - фильтр валидатор JWT токена;
    - [SetRequestHeaderFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/SetRequestHeaderFilter.java) - фильтр передатчик (задатчик) заголовка "Authorization" в request запроса и загрузки туда токена;
    - [JwtSaver.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/utils/JwtSaver.java) - файл сохраняющий JWT токен на стороне сервиса (ЧЕГО В ПРИНЦИПЕ НЕНАДО ДЕЛАТЬ!)
  - [CustomHttpServletRequestWrapper.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/http_servlet_wrapper/CustomHttpServletRequestWrapper.java) - обертка над стандартным HttpServletRequest, которая позволяет добавлять в запрос любые наши параметры (например "Authorization");
  - [mapper](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/mapper) - файлы мапперы для преобразования одних классов в другие (Entity в DTO и обратно);
  - [models](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/models) - все наши ключевые сущности;
  - [repository](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/repository) - интерфейсы взаимодействия с БД;
  - [services](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/services) - слой бизнес логики (прослойка между контролерами и грубо БД);
  - [validation](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/java/me/oldboy/validation) - раздел с валидаторами входящих данных;
  - [SecurityDemoAppPart_9_1.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/SecurityDemoAppPart_9_1.java) - запускаемый файл сервиса (приложения);
- front-end раздел (templates и css):
  - [css](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/resources/static/css) - файлы стилей;
  - [templates](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_9_1/src/main/resources/templates) - файлы отображений;
________________________________________________________________________________________________________________________
### Логика работы приложения:

Еще раз, большинство "книжных" примеров рассматривают вариант использования JWT токена в REST сервисах, есть варианты, 
когда front-end сторона отдельный модуль (самостоятельное приложение), которая взаимодействует с REST сервисом и после 
аутентификации уже на своей стороне хранит сгенерированный REST сервисом токен безопасности (еще раз задача сервиса, 
сгенерировать токен, отдать клиенту и после только проверять подлинность). Но мы сделали "вещь в себе".

Этапы работы приложения:
- Шаг 1. - клиент обращается к web интерфейсу сервиса любой endpoint - http://localhost:8080/webui/ ;
- Шаг 2. - поскольку у нас активна система безопасности сервис перебрасывает клиента на страницу (endpoint) аутентификации - http://localhost:8080/webui/login (работает метод [*.clientLoginPage()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java#L48) класса [LoginRegController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java) и view форма [login.html](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/resources/templates/client_forms/login.html));
  - Шаг 2.1 - если пользователь еще не зарегистрирован, он может зарегистрироваться в сервисе перейдя на - http://localhost:8080/webui/registration (работает метод [*.regClientPage()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java#L114) класса [LoginRegController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java) и view форма [registration.html](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/resources/templates/client_forms/registration.html)) после процедуры регистрации он автоматически вернется в форму аутентификации;   
  - Шаг 2.2 - если пользователь ранее уже регистрировался - просто вводит учетные данные и ... дальше начинается самое интересное;
- Шаг 3. - сервис проверят учетные данные (если данные не верны в форме аутентификации просто появиться сообщение об этом), если же все в порядке, то клиент переходит на - http://localhost:8080/webui/jwt_token (работает метод [*.getJwtAndContinue()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java#L66) класса [LoginRegController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java) и view форма [continue.html](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/resources/templates/continue.html));

На третьем шаге нашему back-end сервису приходится заняться работой front-end сервиса (приложения) и сохранить сгенерированный 
JWT токен (работает метод [*.saveJwtToken()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/utils/JwtSaver.java#L24) класса [JwtSaver](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/utils/JwtSaver.java)). 

При этом логика обращения с JWT token-ом не нарушена: обращение к сервису -> аутентификация -> получение JWT token-a.

Нужно помнить, что все это время мы незримо проходили через цепочку Spring Security:

        DisableEncodeUrlFilter
        WebAsyncManagerIntegrationFilter
        SecurityContextHolderFilter
        HeaderWriterFilter
        CorsFilter
        LogoutFilter
        SetRequestHeaderFilter - наш фильтр перебрасывающий JWT из response в request
        JwtTokenValidatorAndBeforeFilter - наш фильтр валидирующий полученный в requst-e JWT
        UsernamePasswordAuthenticationFilter
        JwtTokenGeneratorAndAfterFilter - наш фильтр генерирующий JWT и возвращающий его в response (в заголовке "Authorization")
        RequestCacheAwareFilter
        SecurityContextHolderAwareRequestFilter
        AnonymousAuthenticationFilter
        SessionManagementFilter
        ExceptionTranslationFilter
        AuthorizationFilter

И так, если учетные данные верны происходит следующее (где-то в глубинах третьего шага):
- Шаг 3.1 - в момент проверки данных в цепи фильтров безопасности подключается UsernamePasswordAuthenticationFilter и проделывает необходимые операции проверки;
- Шаг 3.2 - в момент успешной аутентификации наш перехватчик процесса AuthenticationEventListener отображает результат в консоли и самое главное - запоминает учетные данные (без пароля), поскольку у нас нет сессии их надо где-то хранить;
- Шаг 3.3 - сохраненные данные об аутентификации попадают в наш фильтр генератор JWT - [JwtTokenGeneratorAndAfterFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/JwtTokenGeneratorAndAfterFilter.java);
- Шаг 3.4 - фильтр генерирует JWT токен и возвращает в response клиенту в заголовке "Authorization";

На странице отображения клиент видит полученный JWT token и может продолжать работу. Поскольку мы старались сохранить 
логику взаимодействия front-end и back-end модулей, как будто они самостоятельные части, то далее происходит следующее:  
- Шаг 4. - со страницы [continue.html](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/resources/templates/continue.html) методом POST полученный JWT token пробрасывается на основную страницу - http://localhost:8080/webui/main (работает метод [*.postMainPage()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java#L99) класса [LoginRegController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java) и view форма [main.html](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/resources/templates/main.html));

Самое любопытное в данное ситуации, то, что со страницы [continue.html](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/resources/templates/continue.html) мы пробрасываем JWT token в теле запроса, но у нас
нет возможности "просто, на лету" взять и поместить token в нужный заголовок запроса, а затем проделывать этот фокус каждый 
раз при обращении к нужному endpoint-у (к нужной странице). 

Поэтому мы делаем следующее:
- Шаг 5. - в методе [*.getJwtAndContinue()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java#L66) класса [LoginRegController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LoginRegController.java) происходит запоминание полученного из response JWT token-a - [*.saveJwtToken()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/utils/JwtSaver.java#L24) класса [JwtSaver](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/utils/JwtSaver.java) и только за тем перенаправление на - http://localhost:8080/webui/main;
- Шаг 6. - поскольку перенаправление это следующий запрос, то снова в работу включается цепь безопасности и подключается наш фильтр [SetRequestHeaderFilter](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/SetRequestHeaderFilter.java), который записывает в request header "Authorization" полученный ранее JWT token;
- Шаг 7. - далее по цепи фильтров находится наш фильтр JWT валидатор - [JwtTokenValidatorAndBeforeFilter.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/filters/JwtTokenValidatorAndBeforeFilter.java), который достает из полученного request header-a "Authorization" ранее полученный JWT token, парсит его и устанавливает объект аутентификации в контекст безопасности;
- Шаг 8. - клиент получает доступ к требуемой странице (нужному endpoint-у);

При этом, логика обращения с JWT token-ом, после его получения от сервиса, снова не нарушена: обращение к нужному endpoint-у 
сервиса с передачей JWT token-a в заголовке запроса "Authorization" -> проверка подлинности token-a -> возвращение данных от 
сервиса по полученному запросу. Все шаги повторяются при каждом запросе - как "по учебнику".

Правда с нашим приложением есть одно маленькое "НО". 

Дело в том, что JWT токен, в реальности, храниться на стороне front-end приложения и back-end сервис не должен хранить 
его - см. теорию выше - только проверять подлинность. Т.е. фокус с разлогиневанием (условно, отменой ранее выданного 
токена) в реальной системе реализовать будет интересной задачей - вариант сменить секретный ключ, так себе решение, 
т.к. "упадут" сразу все ранее выданные и еще "живые" токены - это тоже было упомянуто выше. Да, нашим приложением мы 
сломали всю логику применения JWT токен, однако... 

Так, ну, а при чем тут "НО" - в нашей монолитной системе мы храним токен, а значит легко можем удалить только его. И 
значит, у нас легко реализовать полноформатный функционал "разлогиневания" - cм. реализацию [LogoutController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_9_1/src/main/java/me/oldboy/controllers/webui/LogoutController.java).

Ну, круто же! Хотя, если честно при определенной реализации front-end блока, "разлогинивание" в прямом его виде с JWT 
все же не работает - выписанный token живет пока не истечет, как бы мы не мечтали, и об этом в продолжении...
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