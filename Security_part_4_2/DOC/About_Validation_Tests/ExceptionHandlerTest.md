### Тестирование @ExceptionHandler в Spring контроллерах

Для тестирования методов, аннотированных **@ExceptionHandler** в Spring контроллерах, можно использовать несколько 
подходов. Основные методы:
________________________________________________________________________________________________________________________
**1. Использование MockMvc (интеграционное тестирование)**

           @SpringBootTest
           @AutoConfigureMockMvc
           class ExceptionHandlerTest {
            
               @Autowired
               private MockMvc mockMvc;
            
               @Test
               void whenResourceNotFound_thenReturns404() throws Exception {
                   mockMvc.perform(get("/api/resource/999"))
                          .andExpect(status().isNotFound())
                          .andExpect(jsonPath("$.message").value("Resource not found"));
               }
           }

________________________________________________________________________________________________________________________
**2. Тестирование только контроллера (без поднятия контекста)**

           @WebMvcTest(MyController.class)
           class MyControllerTest {
        
               @Autowired
               private MockMvc mockMvc;
            
               @MockBean
               private MyService myService;
            
               @Test
               void whenInvalidInput_thenReturns400() throws Exception {
                   when(myService.process(any())).thenThrow(new IllegalArgumentException("Invalid input"));
                
                        mockMvc.perform(post("/api/process")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content("{\"invalid\":\"data\"}"))
                               .andExpect(status().isBadRequest())
                               .andExpect(jsonPath("$.error").value("Invalid input"));
               }
           }

________________________________________________________________________________________________________________________
**3. Тестирование самого ExceptionHandler**

Если ваш @ExceptionHandler вынесен в отдельный @ControllerAdvice:

        @RestControllerAdvice
        public class GlobalExceptionHandler {
        
            @ExceptionHandler(ResourceNotFoundException.class)
            @ResponseStatus(HttpStatus.NOT_FOUND)
            public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
                return new ErrorResponse("Not Found", ex.getMessage());
            }
        }

Тест для @ControllerAdvice:

        class GlobalExceptionHandlerTest {
        
            private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
            private final MockHttpServletResponse response = new MockHttpServletResponse();
            private final MockHttpServletRequest request = new MockHttpServletRequest();
        
            @Test
            void testResourceNotFoundExceptionHandler() {
                ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
                
                ErrorResponse result = handler.handleResourceNotFound(ex);
                
                assertThat(result.getStatus()).isEqualTo("Not Found");
                assertThat(result.getMessage()).isEqualTo("Resource not found");
            }
        }

________________________________________________________________________________________________________________________
**4. Использование TestRestTemplate (для полных интеграционных тестов)**

           @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
           class ExceptionHandlerIntegrationTest {
        
               @LocalServerPort
               private int port;
            
               @Autowired
               private TestRestTemplate restTemplate;
            
               @Test
               void whenExceptionThrown_thenReturnsCorrectResponse() {
                   ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                       "http://localhost:" + port + "/api/failing-endpoint",
                       ErrorResponse.class);
            
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody().getMessage()).isNotNull();
               }
           }

________________________________________________________________________________________________________________________
**Советы по тестированию ExceptionHandler:**

- Проверяем не только HTTP статус, но и тело ответа
- Тестируем все возможные исключения, которые обрабатывает наш handler
- Для проверки структуры JSON ответа используем JsonPath (как в примерах выше)
- Если наш handler логирует ошибки, можно использовать @SpyBean для проверки вызовов логгера
________________________________________________________________________________________________________________________
Пример проверки структуры ответа:

        mockMvc.perform(get("/invalid"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.timestamp").exists())
               .andExpect(jsonPath("$.status").value(400))
               .andExpect(jsonPath("$.error").value("Bad Request"))
               .andExpect(jsonPath("$.message").value("Invalid request parameters"));

Выбор подхода зависит от того, насколько изолированным или интеграционным должно быть ваше тестирование.