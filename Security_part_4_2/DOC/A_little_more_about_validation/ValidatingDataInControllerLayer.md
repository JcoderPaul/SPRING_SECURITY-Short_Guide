### Проверка (валидация) данных на уровне контроллера Spring

Проверка входящих данных на уровне контроллера Spring имеет решающее значение для поддержания целостности данных и 
безопасности приложения. Вот возможные подходы к реализации проверки:

________________________________________________________________________________________________________________________
**1. Использование Bean Validation (JSR-380)**

Наиболее распространенный подход — использование аннотаций Java Bean Validation:

        @RestController
        @RequestMapping("/users")
        public class UserController {
        
            @PostMapping
            public ResponseEntity<User> createUser(@Valid @RequestBody UserDto userDto) {
                // ... some code
            }
        }

С классом DTO, содержащим аннотации проверки:

        public class UserDto {

            @NotBlank(message = "Name is mandatory")
            private String name;
        
            @Email(message = "Email should be valid")
            private String email;
        
            @Min(value = 18, message = "Age must be at least 18")
            private int age;
            
            // getters and setters
        }

________________________________________________________________________________________________________________________
**2. Использование глобального обработчика ошибок**

Spring автоматически выдает исключение **MethodArgumentNotValidException** при неудачной проверке. Его можно обработать
с помощью **@ExceptionHandler**:

        @RestControllerAdvice
        public class GlobalExceptionHandler {
        
            @ExceptionHandler(MethodArgumentNotValidException.class)
            public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
                
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult()
                  .getAllErrors()
                  .forEach(error -> {String fieldName = ((FieldError) error).getField();
                                     String errorMessage = error.getDefaultMessage();
                                     errors.put(fieldName, errorMessage);
                });
                return ResponseEntity.badRequest().body(errors);
            }
        }

В нашем случае пример реализации, например: [DtoValidationHandler](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/src/main/java/me/oldboy/exception/handlers/DtoValidationHandler.java)

________________________________________________________________________________________________________________________
**3. Пользовательские (custom) валидаторы**

Для сложной логики проверки можно создать пользовательские валидаторы:

        @Target({FIELD})
        @Retention(RUNTIME)
        @Constraint(validatedBy = PhoneNumberValidator.class)
        public @interface ValidPhoneNumber {
            String message() default "Invalid phone number";
            Class<?>[] groups() default {};
            Class<? extends Payload>[] payload() default {};
        }

Реализация данного валидатора:

        public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
        @Override
        public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
                return phoneNumber != null && phoneNumber.matches("\\d{10}");
            }
        }

В нашем случае, вариант аннотации валидатора - [CheckDetails](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/src/main/java/me/oldboy/validation/annitation/CheckDetails.java),
сама реализация валидатора - [DetailsValidator](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/src/main/java/me/oldboy/validation/validator/DetailsValidator.java),
применение кастомного валидатора в коде для отслеживания ошибок - [ClientCreateDto см. ст. 16](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_4_2/src/main/java/me/oldboy/dto/client_dto/ClientCreateDto.java#L16)

________________________________________________________________________________________________________________________
**4. Программная проверка**

Для случаев, когда аннотаций недостаточно:

        @PostMapping
        public ResponseEntity<?> createSomething(@RequestBody SomeDto dto) {
            if (someComplexCondition(dto)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed");
            }
            // proceed if valid
        }

________________________________________________________________________________________________________________________
**5. Групповая проверка**

Проверка различных сценариев с группами:

        public interface BasicValidation {}
        public interface AdvancedValidation {}
        
        public class UserDto {
            @NotBlank(groups = BasicValidation.class)
            private String username;
        
            @NotBlank(groups = AdvancedValidation.class)
            private String password;
        }
        
        @PostMapping("/basic")
        public ResponseEntity<?> basicCreate(@Validated(BasicValidation.class) @RequestBody UserDto dto) {
            // ...
        }

________________________________________________________________________________________________________________________
**Рекомендации:**
- Проверяем данные на ранней стадии, лучше на уровне контроллера перед обработкой.
- Используем DTO, создаем отдельные DTO для проверки ввода.
- "Значительные" сообщения, генерируем понятные сообщения об ошибках.
- "Последовательные" ответы, стандартизируем формат ответа об ошибках.
- Комбинируем подходы, используем аннотации для простых правил и настраиваемые валидаторы для сложной логики.

Не забываем включить необходимые зависимости в свой проект (например, spring-boot-starter-validation для Spring Boot).