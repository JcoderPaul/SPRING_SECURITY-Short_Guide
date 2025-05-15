Если мы работаем со Spring без Boot (например, в legacy-проектах или кастомных конфигурациях, в нашем случае это 
[Security_part_3_3](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/master/Security_part_3_3) и другие в этом справочнике с пометкой (non Boot) в описании), тесты (и интеграционные, в том числе) пишутся иначе, чем в Spring Boot. Здесь нет "автомагических" аннотаций 
таких как @SpringBootTest, и контекст Spring настраивается вручную. 

Разберём ключевые подходы:
________________________________________________________________________________________________________________________
#### 1. Базовый интеграционный тест (без Boot):

Вместо @SpringBootTest контекст создаётся вручную через @ContextConfiguration.

        @RunWith(SpringJUnit4ClassRunner.class)  // или SpringRunner в JUnit 5
        @ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
        public class UserServiceIntegrationTest {
        
            @Autowired
            private UserService userService;
        
            @Autowired
            private UserRepository userRepository;
        
            @Test
            public void shouldSaveAndRetrieveUser() {
                User user = new User("test@example.com");
                userService.save(user);
        
                User found = userRepository.findByEmail("test@example.com");
                assertNotNull(found);
            }
        }
Где: 
- **AppConfig.class** и **DatabaseConfig.class** - наши Java/XML - конфигурации Spring.
- **@RunWith** (с JUnit 4) или **@ExtendWith(SpringExtension.class)** (с JUnit 5) — запускает тест в контексте Spring.
________________________________________________________________________________________________________________________
#### 2. Тестирование с XML-конфигурацией:
Если Spring настроен через XML (старые проекты), используем файл конфигурации:

        @ContextConfiguration(locations = "/applicationContext.xml")
        public class XmlConfigIntegrationTest {
        // ...
        }
________________________________________________________________________________________________________________________
#### 3. Тестирование веб-слоя (без Spring Boot):
Для тестирования контроллеров без Boot можно использовать:
- **MockMvc** (как в Boot, но с ручной настройкой);
- **TestRestTemplate** с запущенным сервером;

Пример с MockMvc:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = {WebConfig.class, ServiceConfig.class})
        @WebAppConfiguration  // Важно для MVC-тестов!
        public class UserControllerTest {
        
            @Autowired
            private WebApplicationContext webAppContext;
        
            private MockMvc mockMvc;
        
            @Before
            public void setup() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
            }
        
            @Test
            public void shouldReturnUser() throws Exception {
                mockMvc.perform(get("/user/1"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.name").value("Test User"));
            }
        }
________________________________________________________________________________________________________________________
#### 4. Тестирование с транзакциями:
Для отката изменений в БД после теста:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = DatabaseConfig.class)
        @Transactional  // Откатывает транзакцию после теста при определенных настройках
        public class TransactionalTest {
        
            @Autowired
            private UserRepository userRepository;
        
            @Test
            public void testSaveUser() {
                User user = new User("test@example.com");
                userRepository.save(user);
                // Код для удаления данных после теста из БД
            }
        }
________________________________________________________________________________________________________________________
#### 5. Тестирование с Testcontainers (БД в Docker)
Если используется PostgreSQL, MySQL и т.д.:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = TestDatabaseConfig.class)
        public class TestcontainersIntegrationTest {
        
            @ClassRule  // Для JUnit 4
            public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
        
            @Autowired
            private DataSource dataSource;
        
            @Test
            public void shouldConnectToDb() throws SQLException {
                assertNotNull(dataSource.getConnection());
            }
        }

Где: **TestDatabaseConfig.class** настраивает **DataSource** на URL из контейнера:

        @Configuration
        public class TestDatabaseConfig {
            @Bean
            public DataSource dataSource() {
                return DataSourceBuilder.create()
                                        .url(postgres.getJdbcUrl())
                                        .username(postgres.getUsername())
                                        .password(postgres.getPassword())
                                        .build();
            }
        }
________________________________________________________________________________________________________________________
#### 6. Тестирование JPA (без @DataJpaTest):
Вместо @DataJpaTest настраиваем EntityManager и транзакции вручную:

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = JpaConfig.class)
        @DataJpaTest  // Недоступно без Boot! Используем @Transactional + @Autowired EntityManager
        @Transactional
        public class JpaIntegrationTest {
        
            @Autowired
            private EntityManager entityManager;
        
            @Test
            public void shouldPersistUser() {
                User user = new User("test@example.com");
                entityManager.persist(user);
                entityManager.flush();
        
                User found = entityManager.find(User.class, user.getId());
                assertEquals("test@example.com", found.getEmail());
            }
        }
________________________________________________________________________________________________________________________
#### Ключевые отличия от Spring Boot:

| Фича	              | Spring без Boot                        | Spring Boot                      |
|--------------------|----------------------------------------|----------------------------------|
| Загрузка контекста | @ContextConfiguration + ручные конфиги | @SpringBootTest                  |
| Веб-тесты          | @WebAppConfiguration + ручной MockMvc  | @AutoConfigureMockMvc            |
| JPA-тесты          | Ручная настройка EntityManager         | @DataJpaTest                     |
| Тестовые БД        | H2 вручную или Testcontainers          | @DataJpaTest + автоматическая H2 |
________________________________________________________________________________________________________________________

#### Итог:
Для **Spring без Boot**:
1. Контекст настраивается через **@ContextConfiguration**.
2. **MockMvc** и транзакции требуют ручной настройки.
3. Для БД используются либо in-memory (H2), либо Testcontainers.
4. Нет "автоматических" аннотаций вроде **@DataJpaTest** — всё конфигурируется вручную.
________________________________________________________________________________________________________________________
Примеры применения рабочих тестов можно найти тут - [test](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/tree/04608aac9d483b2b6ef99ae695faabaf6497fa19/Security_part_4_1/src/test).