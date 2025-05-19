### Примеры использования BindingResult в Spring

BindingResult - это интерфейс в Spring Framework, который хранит результаты валидации и привязки данных. Вот несколько 
примеров его использования:

________________________________________________________________________________________________________________________
**1. Базовая валидация формы**

           @PostMapping("/register")
           public String registerUser(@Valid 
                                      @ModelAttribute("user") User user,
                                      BindingResult bindingResult) {
        
               if (bindingResult.hasErrors()) {
                   // Возвращаем обратно на форму с ошибками
                   return "registration-form";
               }
            
               // Если ошибок нет, продолжаем обработку
               userService.save(user);
               return "redirect:/success";
           }

________________________________________________________________________________________________________________________
**2. Проверка отдельных полей**

           if (bindingResult.hasFieldErrors("email")) {
               // Обработка ошибок конкретного поля
               List<FieldError> emailErrors = bindingResult.getFieldErrors("email");
               // ...
           }

________________________________________________________________________________________________________________________
**3. Кастомная валидация**

           @PostMapping("/transfer")
           public String transferMoney(@Valid 
                                       @ModelAttribute("transfer")
                                       TransferDto transfer,
                                       BindingResult bindingResult) {
        
               // Кастомная проверка
               if (transfer.getAmount() <= 0) {
                    bindingResult.rejectValue("amount", "error.amount", "Amount must be positive");
               }
            
               if (bindingResult.hasErrors()) {
                    return "transfer-form";
               }
            
               accountService.transfer(transfer);
               return "redirect:/success";
           }

________________________________________________________________________________________________________________________
**4. Глобальные ошибки (не привязанные к полю)**

            @PostMapping("/login")
            public String login(@Valid 
                                @ModelAttribute("login") LoginForm loginForm,
                                BindingResult bindingResult) {
            
                // Check credentials (example)
                if (!authService.isValidCredentials(loginForm.getEmail(), loginForm.getPassword())) {
                    bindingResult.reject("invalid.credentials", "Email or password is incorrect");
                }
            
                if (bindingResult.hasErrors()) {
                    return "login-page";
                }
            
                return "redirect:/dashboard";
            }

Отображение глобальных ошибок в Thymeleaf (html):

            <div th:if="${#fields.hasGlobalErrors()}">
                <ul>
                    <li th:each="err : ${#fields.globalErrors()}" 
                        th:text="${err}" class="global-error"></li>
                </ul>
            </div>
________________________________________________________________________________________________________________________
**5. Получение и обработка всех ошибок при валидации**

           if (bindingResult.hasErrors()) {
               List<ObjectError> allErrors = bindingResult.getAllErrors();
               for (ObjectError error : allErrors) {
                    System.out.println(error.getDefaultMessage());
               }
           }

________________________________________________________________________________________________________________________
**6. Использование в Thymeleaf**

В HTML-шаблоне (Thymeleaf):

            <form th:object="${user}" method="post">
                <input type="text" th:field="*{email}" />
                <span th:if="${#fields.hasErrors('email')}" 
                      th:errors="*{email}" class="error"></span>
            
                <input type="password" th:field="*{password}" />
                <span th:if="${#fields.hasErrors('password')}" 
                      th:errors="*{password}" class="error"></span>
                
                <button type="submit">Submit</button>
            </form>

________________________________________________________________________________________________________________________
**7. Проверка вложенных объектов**

При проверке объектов внутри объектов (например, у пользователя есть адрес):

- Классы сущностей:
        
        public class User {
            @NotBlank
            private String name;
        
            @Valid  // Required to validate nested object
            private Address address;
        }

        public class Address {
            @NotBlank
            private String street;
        }

- Контроллер:

        @PostMapping("/saveUser")
        public String saveUser(@Valid 
                               @ModelAttribute("user") User user,
                               BindingResult bindingResult) {
        
            if (bindingResult.hasErrors()) {
                return "user-form";
            }
            // ... save logic
        }

- Thymeleaf (доступ к вложенным ошибкам):

        <input th:field="*{address.street}" />
        <span th:errors="*{address.street}" class="error"></span>

________________________________________________________________________________________________________________________
**Обязательно:** BindingResult всегда должен идти сразу после объекта, который валидируется (с аннотацией @Valid), иначе 
Spring выбросит исключение.
________________________________________________________________________________________________________________________
**Ключевые моменты:**
- Порядок имеет значение: BindingResult должен следовать сразу за параметром объекта @Valid.
- Интеграция Thymeleaf: используем #fields.hasErrors() и th:errors для отображения ошибок.
- Пользовательские ошибки: используем reject() для глобальных ошибок или rejectValue() для ошибок, специфичных для поля.
- Вложенная проверка: аннотируем вложенные объекты с помощью @Valid для каскадной проверки.
________________________________________________________________________________________________________________________
Более подробную информацию можно найти в [документации по проверке Spring.](https://docs.spring.io/spring-framework/reference/core/validation.html)