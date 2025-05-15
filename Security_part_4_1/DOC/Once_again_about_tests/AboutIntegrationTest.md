**Интеграционные тесты в Spring Java** – это тесты, которые проверяют взаимодействие нескольких компонентов приложения 
(сервисов, репозиториев, контроллеров, БД и внешних сервисов) в связке. В отличие от модульных тестов, где тестируются 
отдельные классы изолированно, интеграционные тесты проверяют, как разные части системы работают вместе.
________________________________________________________________________________________________________________________
#### Особенности интеграционных тестов в Spring
1. Тестирование в контексте Spring:
   - Загружается часть или весь Spring-контекст (ApplicationContext).
   - Используются аннотации Spring для настройки тестов.
2. Работа с реальными или тестовыми зависимостями:
   - Могут подниматься in-memory БД (H2), мокироваться внешние API (MockMvc, Mockito) или использоваться тестовые профили (@ActiveProfiles("test")).
3. Проверка интеграции с БД, HTTP-запросами и другими сервисами:
   - Например, тестируются JPA-репозитории, REST-контроллеры или цепочки вызовов между сервисами.
________________________________________________________________________________________________________________________
#### Ключевые аннотации для интеграционных тестов:

| Аннотация              | Описание                                                                     |
|------------------------|------------------------------------------------------------------------------|
| @SpringBootTest        | Загружает полный или частичный контекст Spring (как при запуске приложения). |
| @DataJpaTest           | Тестирует JPA-репозитории с настройкой in-memory БД (H2).                    |
| @WebMvcTest            | Тестирует только слой контроллеров (MVC), сервисы мокируются.                |
| @Testcontainers        | Запускает тесты в Docker-контейнерах (например, PostgreSQL, Redis).          |         
| @Transactional         | Откатывает изменения в БД после теста (чтобы тесты не влияли друг на друга). | 
| @AutoConfigureMockMvc  | Позволяет тестировать REST-эндпоинты через MockMvc.                          | 

________________________________________________________________________________________________________________________
#### Примеры интеграционных тестов:

**1. Тест репозитория с H2 БД:**

         @DataJpaTest
         public class UserRepositoryTest {
         
             @Autowired
             private UserRepository userRepository;
         
             @Test
             void shouldFindUserByEmail() {
                 User user = new User("test@example.com", "password");
                 userRepository.save(user);
         
                 User found = userRepository.findByEmail("test@example.com");
                 assertThat(found.getEmail()).isEqualTo(user.getEmail());
             }
         }

**2. Тест REST-контроллера с MockMvc:**

         @WebMvcTest(UserController.class)
         public class UserControllerTest {
         
             @Autowired
             private MockMvc mockMvc;
         
             @MockBean
             private UserService userService;
         
             @Test
             void shouldReturnUser() throws Exception {
                 when(userService.getUser(1L)).thenReturn(new User("test@example.com", "123"));
         
                 mockMvc.perform(get("/api/users/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value("test@example.com"));
             }
         }         


**3. Полноценный интеграционный тест с @SpringBootTest:**

         @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
         @Testcontainers
         public class UserIntegrationTest {
         
             @Container
             static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
         
             @Autowired
             private TestRestTemplate restTemplate;
         
             @Test
             void shouldCreateUser() {
                 User user = new User("test@example.com", "password");
                 ResponseEntity<User> response = restTemplate.postForEntity("/api/users", user, User.class);
         
                 assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                 assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
             }
         }
________________________________________________________________________________________________________________________
#### Чем отличаются от модульных тестов?

| Критерий    | Модульные тесты  | Интеграционные тесты             |
|-------------|------------------|----------------------------------|
| Объем       | Один класс/метод | Несколько компонентов            |
| Зависимости | Моки (Mockito)   | Реальные или тестовые сервисы/БД |
| Скорость    | Быстрые          | Медленные (из-за контекста)      |		
| Аннотации   | @Test (JUnit)    | @SpringBootTest, @DataJpaTest    |   	

________________________________________________________________________________________________________________________
#### Зачем нужны интеграционные тесты?

1. Проверяют, что компоненты корректно работают вместе.
2. Выявляют проблемы, невидимые в модульных тестах (например, настройки Spring, транзакции, SQL-запросы).
3. Дают уверенность в работоспособности критических сценариев.

________________________________________________________________________________________________________________________
#### Вывод:
Интеграционные тесты в Spring – мощный инструмент для проверки взаимодействия частей приложения. Они дополняют модульные 
тесты, повышая надежность системы. Для эффективности важно правильно настраивать тестовый контекст и использовать 
специализированные аннотации Spring.

________________________________________________________________________________________________________________________
Примеры интеграционных тестов этого проекта - [integration](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/04608aac9d483b2b6ef99ae695faabaf6497fa19/Security_part_4_1/src/test/java/me/oldboy/integration).