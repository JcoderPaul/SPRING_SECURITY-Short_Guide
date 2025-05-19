При тестировании методов контроллера, включающих **@ExceptionHandler**, нам необходимо проверить две ситуации - выброс
ошибки и нормальную работу кода. Варианты подходов:
________________________________________________________________________________________________________________________
**1. Модульное тестирование с помощью MockMvc (рекомендуемый подход)**

        @WebMvcTest(MyController.class)
        class MyControllerTest {
        
            @Autowired
            private MockMvc mockMvc;
        
            @MockBean
            private MyService myService;
        
            @Test
            void validRequest_shouldReturnOk() throws Exception {
                when(myService.process(any(ValidRequest.class))).thenReturn(expectedResponse);
                
                mockMvc.perform(post("/api/process")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"valid\":\"data\"}"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.result").value("success"));
            }
        
            @Test
            void invalidRequest_shouldTriggerExceptionHandler() throws Exception {
                when(myService.process(any())).thenThrow(new InvalidRequestException("Invalid data"));
                
                mockMvc.perform(post("/api/process")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"invalid\":\"data\"}"))
                       .andExpect(status().isBadRequest())
                       .andExpect(jsonPath("$.error").value("Invalid Request"))
                       .andExpect(jsonPath("$.message").value("Invalid data"));
            }
        }

________________________________________________________________________________________________________________________
**2. Тестирование ExceptionHandler напрямую**

Если у нас есть отдельный класс @ControllerAdvice:

        @RestControllerAdvice
        public class GlobalExceptionHandler {
            @ExceptionHandler(InvalidRequestException.class)
                public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid Request", ex.getMessage()));
            }
        }

Тест:

        class GlobalExceptionHandlerTest {
        private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
            @Test
            void shouldHandleInvalidRequestException() {
                InvalidRequestException ex = new InvalidRequestException("Test error");
                ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(ex);
                
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody().getError()).isEqualTo("Invalid Request");
                assertThat(response.getBody().getMessage()).isEqualTo("Test error");
            }
        }
________________________________________________________________________________________________________________________
**3. Полное интеграционное тестирование с TestRestTemplate (SpringBoot)**

        @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
        class MyControllerIntegrationTest {
        
            @LocalServerPort
            private int port;
        
            @Autowired
            private TestRestTemplate restTemplate;
        
            @Test
            void whenValidRequest_thenSuccessResponse() {
                ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/process",
                    new ValidRequest("good data"),
                    String.class);
                
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            }
        
            @Test
            void whenInvalidRequest_thenErrorResponse() {
                ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/process",
                    new InvalidRequest("bad data"),
                    ErrorResponse.class);
                
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody().getError()).isNotNull();
            }
        }

________________________________________________________________________________________________________________________
**4. Тестирование пользовательских аннотаций с помощью ExceptionHandler**
   
Если вы используете пользовательские аннотации, такие как **@Validated**:

        @RestController
        @Validated
        public class MyController {
            @GetMapping("/validate")
            public String validateRequest(@RequestParam @Min(1) int id) {
                return "Valid ID: " + id;
            }
        }

Тест обработки исключений:

        @Test
        void whenInvalidParam_thenConstraintViolationException() throws Exception {
            mockMvc.perform(get("/validate").param("id", "0"))
                   .andExpect(status().isBadRequest())
                   .andExpect(jsonPath("$.errors[0]").value("must be greater than or equal to 1"));
        }

________________________________________________________________________________________________________________________
**Лучшие практики:**

- Проверяем как успешные, так и ошибочные сценарии для каждого endpoint-a
- Проверяем структуру ответа, включая код состояния, заголовки и тело
- Проверяем все пользовательские исключения, которые обрабатывают наши обработчики
- Используем **@WebMvcTest** для уровня контроллера, чтобы тесты оставались сфокусированными (для SpringBoot)
- По возможности используем AssertJ для более читаемых утверждений
- Проверяем порядок обработчиков исключений, если у нас есть несколько обработчиков

________________________________________________________________________________________________________________________
**Пример с несколькими обработчиками исключений**

        @RestControllerAdvice
        public class GlobalExceptionHandler {
            
            @ExceptionHandler(ResourceNotFoundException.class)
            public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Not Found", ex.getMessage()));
            }
        
            @ExceptionHandler(MethodArgumentNotValidException.class)
            public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
                List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                                                                            .map(FieldError::getDefaultMessage)
                                                                            .collect(Collectors.toList());
                
                return ResponseEntity.badRequest().body(new ErrorResponse("Validation Failed", errors.toString()));
            }
        }

Тестируем обы обработчика:

        @Test
        void notFoundException_shouldReturn404() throws Exception {
            when(myService.getResource(anyLong())).thenThrow(new ResourceNotFoundException("Not found"));
        
            mockMvc.perform(get("/api/resources/123"))
                   .andExpect(status().isNotFound())
                   .andExpect(jsonPath("$.error").value("Not Found"));
        }
        
        @Test
        void invalidDto_shouldReturnValidationErrors() throws Exception {
            mockMvc.perform(post("/api/resources")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{}")) // empty/invalid request
                   .andExpect(status().isBadRequest())
                   .andExpect(jsonPath("$.error").value("Validation Failed"))
                   .andExpect(jsonPath("$.message").exists());
        }