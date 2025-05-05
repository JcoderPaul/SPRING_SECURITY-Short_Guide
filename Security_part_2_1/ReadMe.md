### Simple Spring Boot Security App (Step 2_1) - частичное ограничение доступа к ресурсам приложения (Light legacy).

- Spring Boot 2.6.15
- Spring Core 5.6.10
- Spring Security 5.6.10
- Java 8
- Maven

Следующий шаг. Мы добавили 6-ть страниц, часть из которых открыта всем, а часть доступна только после аутентификации.
Пароль и логин для доступа к закрытым страницам, через форму аутентификации (см. application.yml). Пара "ключ:значение" 
определяющие доступ заранее определены, название ключа взяты из документации, значение, естественно, задаем мы или 
Spring при запуске приложения предложит свой случайно сгенерированный. Используется относительно старая кодовая база.

________________________________________________________________________________________________________________________
### Тестирование.

Теперь мы можем увидеть, чем отличаются тесты защищенных endpoint-ов от не защищенных, а так же продемонстрировать 
различия в откликах при симуляции передачи аутентифицированного пользователя в тестовый метод и при ее отсутствии.
Примеры тестирования простых методов контролеров - [controller](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_2_1/src/test/java/me/oldboy/controller).

### Немного теории (@SpringBootTest, @AutoConfigureMockMvc, @WebMvcTest, @WithMockUser):

**@SpringBootTest** - фундаментальная аннотация в Spring Boot. Она применяется на уровне класса в наших интеграционных 
тестах (хотя в данной части о них еще рано говорить). Обычно мы размещаем эту аннотацию над нашим тестовым классом.

Когда мы запускаем тестовый класс, аннотированный с помощью @SpringBootTest, Spring Boot делает следующее:
- Загружает весь контекст приложения Spring: это означает, что он попытается найти наш основной класс приложения (тот, 
который аннотирован @SpringBootApplication) и запустить полную среду Spring, включая все наши компоненты, конфигурации 
и зависимости.
- Предоставляет полностью настроенный ApplicationContext: этот контекст затем доступен для взаимодействия с нашими тестами. 
Мы можем внедрить компоненты из этого контекста в свой тестовый класс с помощью @Autowired.
- Позволяет нам тестировать интеграцию различных частей нашего приложения: поскольку загружен полный контекст, мы можем 
тестировать, как наши controllers взаимодействуют с нашими же services, как наши сервисы взаимодействуют с нашими repository
и т. д.

Можно представить, что @SpringBootTest запускает мини-версию нашего приложения для целей тестирования. Это позволяет нам 
выполнять сквозные или интеграционные тесты, проверяя, что различные компоненты нашего приложения работают вместе правильно.

Простой пример - [SpringSecurityPart2AppTests.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_1/src/test/java/me/oldboy/SpringSecurityPart2AppTests.java)

В приведенном выше примере @SpringBootTest гарантирует, что весь контекст приложения загружается при запуске SpringSecurityPart2AppTests. 
Затем мы "подгружаем" WebApplicationContext при помощи @Autowired для проверки, что он был успешно создан.

**@AutoConfigureMockMvc** - еще одна аннотация в экосистеме тестирования Spring Boot, часто используемая вместе с @SpringBootTest. 
Она применяется, как и @SpringBootTest, на уровне класса наших интеграционных тестов. Мы размещаем ее прямо над самим тестовым 
классом. @AutoConfigureMockMvc применяется на этапе настройки теста, в частности, когда контекст приложения Spring загружается 
для наших тестов (обычно сначала запускается @SpringBootTest). 

Когда Spring сталкивается с @AutoConfigureMockMvc в нашем тестовом классе, он автоматически настраивает и внедряет экземпляр 
MockMvc в тест.

**MockMvc** — это мощный инструмент для тестирования наших контроллеров Spring MVC без фактического запуска полного 
HTTP-сервера. Он позволяет нам имитировать HTTP-запросы (GET, POST, PUT, DELETE и т. д.) и "утверждать" ответы. Это 
делает наши тесты контроллера более быстрыми и целенаправленными, поскольку мы не имеем дело с сетевыми настройками 
или проблемами работающего сервера.

Хотя мы можем использовать @AutoConfigureMockMvc сам по себе в некоторых конкретных сценариях (например, с @WebMvcTest), 
он чаще всего используется в сочетании с @SpringBootTest. 

Вот почему:
- @SpringBootTest загружает полный контекст приложения: это гарантирует, что наши контроллеры, службы и другие зависимости 
доступны.
- @AutoConfigureMockMvc затем уже использует этот загруженный контекст: он использует информацию в контексте приложения 
для настройки экземпляра MockMvc, гарантируя его правильную настройку для взаимодействия с нашими контроллерами.

Простой пример - [ContactController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_1/src/test/java/me/oldboy/controller/ContactControllerTest.java)

Что мы тут имеем:
- @SpringBootTest загружает контекст приложения;
- @AutoConfigureMockMvc гарантирует, что экземпляр MockMvc будет автоматически создан и внедрен в поле mockMvc;
- Тестовый метод использует mockMvc для имитации GET запроса к "/contact" и утверждает статус ответа, тип содержимого.

**@WebMvcTest** - специализированная аннотация тестирования Spring Boot, ориентированная "исключительно" на тестирование 
веб-слоя нашего приложения. Она применяется, как @SpringBootTest и @AutoConfigureMockMvc на уровне класса наших тестов. 
Мы размещаем ее прямо над определением тестового класса. @WebMvcTest применяется на этапе настройки теста. 

Когда мы запускаем тестовый класс, аннотированный @WebMvcTest, Spring Boot делает следующее:
- Настраивает ограниченный контекст приложения Spring. В отличие от @SpringBootTest, @WebMvcTest не загружает весь контекст 
приложения, вместо этого она специально загружает инфраструктуру Spring MVC и компоненты, связанные с веб-контроллерами. 
Сюда входят компоненты помеченные как @Controller, @RestController, @ControllerAdvice, @JsonComponent, а так же Converter, 
GenericConverter, Filter, WebMvcConfigurer и HandlerMethodArgumentResolver.
- Автоматически настраивает MockMvc аналогично @AutoConfigureMockMvc. @WebMvcTest автоматически предоставляет и настраивает 
экземпляр MockMvc для использования в тестах. Затем мы можем "вызвать" @Autowired экземпляр MockMvc в нашем тестовом классе.
- Исключает другие компоненты. Важно, что компоненты других типов (например, @Service, @Component, @Repository) не загружаются 
по умолчанию при использовании @WebMvcTest. Если наш контроллер зависит от этих компонентов, нам обычно нужно предоставить 
для них реализации фиктивных компонентов с помощью @MockBean.

Зачем и когда использовать @WebMvcTest: Основная причина использования @WebMvcTest — выполнение целенаправленных и эффективных 
тестов нашего веб-слоя. Загружая только необходимые компоненты, наши тесты выполняются быстрее по сравнению с использованием 
@SpringBootTest, который загружает весь контекст приложения.

Обычно мы применяем @WebMvcTest, когда хотите протестировать:
- Логику обработки запросов контроллеров. 
- Сопоставление URL-адресов с методами контроллера.
- Проверку параметров запроса и данных.
- Рендеринг представлений (если мы используем рендеринг на стороне сервера).
- Сериализацию и десериализацию данных JSON или XML.
- Поведение наших обработчиков исключений.

Простой пример - [BalanceControllerTest.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_1/src/test/java/me/oldboy/controller/BalanceControllerTest.java)

В этом примере происходит следующее:
- @WebMvcTest(MyController.class) сообщает Spring о необходимости загрузки только веб-компонентов, в частности [BalanceController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_1/src/main/java/me/oldboy/controller/BalanceController.java).
- MockMvc автоматически настраивается и внедряется.
- Тест использует mockMvc для имитации GET запроса и утверждает ответ, а также проверяет, был ли вызван имитированный метод.

**@WithMockUser** - полезная аннотация из Spring Security Test, которая позволяет нам легко имитировать аутентифицированного 
пользователя для тестирования контроллера. Она применяется, как на уровне класса, так и на уровне метода в наших интеграционных 
тестах:
- Уровень класса: при применении на уровне класса она настраивает фиктивного аутентифицированного пользователя для всех 
методов теста в этом классе.
- Уровень метода: при применении на уровне метода она переопределяет любую конфигурацию @WithMockUser уже стоящую на уровне 
класса (или обеспечивает аутентификацию для определенного метода теста, если аннотация на уровне класса отсутствует).

@WithMockUser обрабатывается во время фазы настройки теста, в частности, перед каждым методом теста, к которому она 
применяется (или для всех методов в классе, если применяется на уровне класса). Когда Spring Security Test встречает эту 
аннотацию, он делает следующее:
- Создает фиктивный объект аутентификации: он конструирует простой объект аутентификации, представляющий аутентифицированного 
пользователя.
- Заполняет SecurityContextHolder: он помещает этот фиктивный объект аутентификации в SecurityContextHolder, который является 
способом, которым Spring Security получает доступ к информации аутентификации текущего пользователя.

Применение данной аннотации эффективно имитирует пользователя, вошедшего в систему и имеющего определенные роли или полномочия, 
что позволяет нам тестировать ограничения безопасности и логику управления доступом в наших контроллерах.

Обычно @WithMockUser используется вместе с @SpringBootTest или @WebMvcTest, когда мы хотим протестировать защищенные конечные 
точки (endpoint-ы).

Простой пример аннотирования на уровне класса - [WelcomeControllerTest.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_1/src/test/java/me/oldboy/controller/WelcomeControllerTest.java)
Простой пример аннотирования на уровне метода - [BalanceControllerTest.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_2_1/src/test/java/me/oldboy/controller/BalanceControllerTest.java)
________________________________________________________________________________________________________________________
### Reference Documentation:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Reference Guide (history)](https://docs.spring.io/spring-boot/docs/)
* [Spring Security](https://spring.io/projects/spring-security)
* [Spring Security Examples](https://spring.io/projects/spring-security#samples)

### About tests documentation:
* [Testing the Web Layer](https://spring.io/guides/gs/testing-web)
* [Testing Spring Boot Applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
* [Running a Test as a User in Spring MVC Test](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/authentication.html)

### Guides:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Spring Guides](https://spring.io/guides)

### About tests article:

* [Using @Autowired and @InjectMocks in Spring Boot Tests](https://www.baeldung.com/spring-test-autowired-injectmocks)
* [How to Test a Secured Spring Web MVC Endpoint with MockMvc](https://www.diffblue.com/resources/how-to-test-a-secured-spring-web-mvc-endpoint-with-mockmvc/)
* [Spring Security for Spring Boot Integration Tests](https://www.baeldung.com/spring-security-integration-tests)
* [Spring Boot WithMockUser parametrisiert (DE)](https://blog.doubleslash.de/software-technologien/coding-and-frameworks/spring-boot-withmockuser-parametrisiert)
* [Parametrize @WithMockUser Spring boot](https://stackoverflow.com/questions/72255932/parametrize-withmockuser-spring-boot/72256754#72256754)
________________________________________________________________________________________________________________________

