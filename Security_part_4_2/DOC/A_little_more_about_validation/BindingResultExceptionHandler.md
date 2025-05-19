В Spring MVC, BindingResult используется для валидации данных формы или DTO объектов. Если возникает ошибка валидации, 
её можно обработать с помощью @ExceptionHandler.

Варианты обработки ошибок валидации с BindingResult через @ExceptionHandler:
________________________________________________________________________________________________________________________
**1. Контроллер с валидацией и BindingResult**
   
Допустим, у нас есть форма, которая принимает UserDto, и мы хотим проверить её на валидность.

            @Controller
            public class UserController {
            
                @PostMapping("/register")
                public String registerUser(@Valid 
                                           @ModelAttribute("user") UserDto userDto,
                                           BindingResult bindingResult) {
            
                    if (bindingResult.hasErrors()) {
                        // Если есть ошибки, возвращаем обратно на форму
                        return "registration-form";
                    }
            
                    // Если валидация прошла успешно, сохраняем пользователя
                    userService.save(userDto);
                    return "redirect:/success";
                }
            }

Пример похожего использования в нашем коде - [ClientController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_1/src/main/java/me/oldboy/controllers/ClientController.java#L65)
________________________________________________________________________________________________________________________
**2. DTO-класс с аннотациями валидации**

           public class UserDto {
               @NotBlank(message = "Имя не может быть пустым")
               private String name;
            
               @Email(message = "Некорректный email")
               private String email;
            
               @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
               private String password;
            
               // Геттеры и сеттеры
           }

В нашем случае варианты применения - [ClientCreateDto.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/src/main/java/me/oldboy/dto/client_dto/ClientCreateDto.java)

________________________________________________________________________________________________________________________
**3. Обработка ошибок валидации через @ExceptionHandler**
   
Если @Valid обнаруживает ошибки, они попадают в BindingResult. Но если валидация не сработала (например, из-за неправильного 
типа данных), Spring выбросит MethodArgumentNotValidException.

Мы можем перехватить её с помощью @ExceptionHandler:

            @ControllerAdvice
            public class GlobalExceptionHandler {
            
                @ExceptionHandler(MethodArgumentNotValidException.class)
                public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
            
                    BindingResult bindingResult = ex.getBindingResult();
                    Map<String, String> errors = new HashMap<>();
            
                    bindingResult.getFieldErrors().forEach(error -> 
                        errors.put(error.getField(), error.getDefaultMessage())
                    );
            
                    return ResponseEntity.badRequest().body(errors);
                }
            }

________________________________________________________________________________________________________________________
**4. Альтернативный вариант: Возврат ModelAndView**

Если нужно вернуть HTML-страницу с ошибками:

            @ExceptionHandler(MethodArgumentNotValidException.class)
            public ModelAndView handleValidationExceptions(MethodArgumentNotValidException ex) {
                BindingResult bindingResult = ex.getBindingResult();
                ModelAndView mav = new ModelAndView("error-page"); // Имя Thymeleaf/HTML шаблона
                
                bindingResult.getFieldErrors().forEach(error -> 
                    mav.addObject(error.getField(), error.getDefaultMessage())
                );
                
                mav.setStatus(HttpStatus.BAD_REQUEST);
                return mav;
            }

________________________________________________________________________________________________________________________
**Применяемость:**
- REST API: Возвращаем ResponseEntity с JSON-ошибками.
- Thymeleaf/JSP формы: Возвращаем ModelAndView с сообщениями об ошибках.
- Глобальная обработка: @ControllerAdvice позволяет перехватывать ошибки во всех контроллерах.
________________________________________________________________________________________________________________________
Краткие выводы:
- BindingResult содержит ошибки валидации.
- Если валидация не прошла, можно обработать MethodArgumentNotValidException через @ExceptionHandler.
- Можно возвращать JSON (ResponseEntity) или HTML (ModelAndView).
________________________________________________________________________________________________________________________
Описанное выше подходит как для Spring Boot, так и для nonBoot Spring MVC.