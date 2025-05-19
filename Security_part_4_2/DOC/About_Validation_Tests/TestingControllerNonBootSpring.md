### Тестирование уровня контроллера применяющего @ExceptionHandler в Spring nonBoot приложениях.

При тестировании контроллеров с обработкой исключений в традиционном приложении Spring (без Spring Boot) нам нужно будет 
настроить среду тестирования по-другому. 
Вот как к этому подойти:
________________________________________________________________________________________________________________________
**1. Базовая настройка с автономным MockMvc**

        public class MyControllerTest {
        
            private MockMvc mockMvc;
        
            @Before
            public void setup() {
                MyController controller = new MyController();
                controller.setService(new MockService()); // Our mock service
                
                // Setup MockMvc with standalone configuration
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                         .setControllerAdvice(new GlobalExceptionHandler()) // Our exception handler
                                         .build();
            }
            
            @Test
            public void testExceptionHandling() throws Exception {
                mockMvc.perform(get("/endpoint-that-throws"))
                       .andExpect(status().isBadRequest())
                       .andExpect(jsonPath("$.error").exists());
            }
        }

________________________________________________________________________________________________________________________
**2. Тестирование с помощью WebApplicationContext**

        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = {AppConfig.class, WebConfig.class}) // Our config classes
        @WebAppConfiguration
        public class ControllerIntegrationTest {
        
            @Autowired
            private WebApplicationContext wac;
            
            private MockMvc mockMvc;
            
            @Before
            public void setup() {
                mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
            }
            
            @Test
            public void testExceptionHandling() throws Exception {
                mockMvc.perform(post("/api/process")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"invalid\":\"data\"}"))
                       .andExpect(status().isBadRequest());
            }
        }

________________________________________________________________________________________________________________________
**3. Тестирование ControllerAdvice отдельно**

        public class GlobalExceptionHandlerTest {
    
            private MockMvc mockMvc;
        
            @Before
            public void setup() {
                // Controller that throws exceptions
                MyController controller = new MyController();
                controller.setService(new ThrowingService());
                
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
            }
            
            @Test
            public void testNotFoundExceptionHandling() throws Exception {
                mockMvc.perform(get("/api/resource/999"))
                       .andExpect(status().isNotFound())
                       .andExpect(jsonPath("$.message").value("Resource not found"));
            }
        }

________________________________________________________________________________________________________________________
**4. Ручной вызов обработчика исключений**

Для непосредственного тестирования логики обработчика:

        public class ExceptionHandlerLogicTest {
            private GlobalExceptionHandler handler = new GlobalExceptionHandler();
            private MockHttpServletRequest request = new MockHttpServletRequest();
            private MockHttpServletResponse response = new MockHttpServletResponse();
        
            @Test
            public void testHandlerLogic() throws Exception {
                Exception ex = new CustomBusinessException("Error occurred");
                ModelAndView mav = handler.handleCustomException(ex, request);
                
                assertThat(mav.getModel().get("error")).isEqualTo("Error occurred");
            }
        }

________________________________________________________________________________________________________________________
**5. Тестирование исключений при валидации**

        public class ValidationExceptionTest {

            private MockMvc mockMvc;
        
            @Before
            public void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(new ValidationController())
                                         .setValidator(new LocalValidatorFactoryBean())
                                         .setControllerAdvice(new GlobalExceptionHandler())
                                         .build();
            }
            
            @Test
            public void testInvalidInput() throws Exception {
                mockMvc.perform(post("/validate")
                       .param("age", "-1")) // Invalid input
                       .andExpect(status().isBadRequest())
                       .andExpect(jsonPath("$.errors").exists());
            }
        }

________________________________________________________________________________________________________________________
**Ключевые отличия от тестирования Spring Boot:**

- Отсутствие автоматической настройки: нам нужно вручную настроить все необходимые компоненты
- Явная настройка контекста: используем **@ContextConfiguration** вместо **@SpringBootTest**
- Ручная настройка MockMvc: **@AutoConfigureMockMvc** недоступен!
- Отсутствие вспомогательных методов **TestRestTemplate**: для полных интеграционных тестов нам нужно будет:
  - Запустить встроенный сервер вручную
  - Настроить DispatcherServlet
  - Выполнить реальные HTTP-вызовы

________________________________________________________________________________________________________________________
**Пример полного интеграционного теста (в Spring nonBoot):**

        public class FullIntegrationTest {
            
            private static EmbeddedWebApplicationContext server;
            private static int port;
        
            @BeforeAll
            public static void startServer() throws Exception {
                server = new EmbeddedWebApplicationContext();
                server.register(WebConfig.class); // Your web configuration
                server.refresh();
                
                HttpServer httpServer = Server.create(port, server.getServletContext());
                httpServer.start();
                port = httpServer.getPort();
            }
            
            @AfterAll
            public static void stopServer() {
                if (server != null) {
                    server.close();
                }
            }
            
            @Test
            public void testEndpointWithException() throws Exception {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/error-endpoint", 
                    Map.class);
                    
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertNotNull(response.getBody().get("error"));
            }
        }

________________________________________________________________________________________________________________________
**Советы по тестированию слоя контроллеров в Spring nonBoot приложений:**
- Модулируем свою настройку: создаем повторно используемые методы конфигурации теста
- Используем Mockito: для имитации зависимостей в автономных тестах
- Можно использовать фреймворк TestContext Spring: для кэширования контекстов приложений
- Упорядочиваем обработчиков исключений теста: если у нас несколько обработчиков
- Проверяем типы медиа-ответов: убеждаемся, что обработчики создают правильный тип контента

________________________________________________________________________________________________________________________
**Особенности** Без автоматической настройки Spring Boot нам нужно будет более явно указывать настройку теста, 
но мы можем получить больший контроль над средой тестирования.