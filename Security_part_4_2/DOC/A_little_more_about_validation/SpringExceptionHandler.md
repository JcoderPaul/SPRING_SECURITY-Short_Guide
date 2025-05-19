В Spring Framework (nonBoot) аннотация @ExceptionHandler используется для обработки исключений внутри контроллера. 
Вот несколько примеров её использования:
________________________________________________________________________________________________________________________
**1. Базовый пример обработки исключения в контроллере**

           @Controller
           public class MyController {
        
               @RequestMapping("/test")
               public void test() {
                    throw new IllegalArgumentException("Ошибка в запросе");
               }
            
               @ExceptionHandler(IllegalArgumentException.class)
               public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
                    return new ResponseEntity<>("Произошла ошибка: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
               }
           }

Здесь метод **handleIllegalArgumentException** будет вызываться, если в контроллере возникнет **IllegalArgumentException**.

________________________________________________________________________________________________________________________
**2. Обработка нескольких исключений**

           @ExceptionHandler({IllegalArgumentException.class, 
                              NullPointerException.class})
           public ResponseEntity<String> handleMultipleExceptions(Exception ex) {
                return new ResponseEntity<>("Произошла ошибка: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
           }

В нашем случае, пример - [ClientDetailsServiceException.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/src/main/java/me/oldboy/exception/handlers/ClientDetailsServiceException.java)   
________________________________________________________________________________________________________________________
**3. Доступ к HttpServletRequest и другим объектам**

           @ExceptionHandler(Exception.class)
           public ResponseEntity<String> handleException(Exception ex, HttpServletRequest request) {
               String errorMessage = "Ошибка при обработке запроса " + request.getRequestURI() + ": " + ex.getMessage();
               return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
           }

В нашем случае, тот же пример см. выше и - [DtoValidationHandler.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/src/main/java/me/oldboy/exception/handlers/DtoValidationHandler.java)
________________________________________________________________________________________________________________________
**4. Использование ModelAndView для возврата представления**

           @ExceptionHandler(RuntimeException.class)
           public ModelAndView handleRuntimeException(RuntimeException ex) {
               ModelAndView mav = new ModelAndView("error");
               mav.addObject("message", ex.getMessage());
               mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
               return mav;
           }
________________________________________________________________________________________________________________________
**5. Глобальная обработка исключений (через @ControllerAdvice)**

Хотя это не в контроллере, но часто используется вместе с @ExceptionHandler:

    @ControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleAllExceptions(Exception ex) {
            return new ResponseEntity<>("Глобальная обработка: " + ex.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

Наши варианты кода см. ссылки выше.
________________________________________________________________________________________________________________________
**Важные моменты:**
- **@ExceptionHandler** работает только в том контроллере, где объявлен.
- Для глобальной обработки нужно использовать **@ControllerAdvice**.
- В non-Boot Spring вам нужно самостоятельно настроить **DispatcherServlet** и другие компоненты.
- Порядок обработки: сначала ищется **@ExceptionHandler** в контроллере, затем в **@ControllerAdvice**.

Эти примеры должны работать в обычном Spring MVC приложении без использования Spring Boot.