### Simple Spring App with Security (Part 10_1) - применение Method Level Security (защита и фильтрация на уровне методов).

- Spring 6.2.2
- Spring Security 6.4.2
- Java 17
- Gradle

________________________________________________________________________________________________________________________
Ключевые аннотации рассмотренные тут:
- [@PreAuthorize](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PreAuthorize.html);
- [@PostAuthorize](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PostAuthorize.html);
- [@PreFilter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PreFilter.html); 
- [@PostFilter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PostFilter.html);
- [@EnableMethodSecurity](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/method/configuration/EnableMethodSecurity.html);

________________________________________________________________________________________________________________________
* [Spring Security Reference](https://docs.spring.io/spring-security/site/docs/5.5.x/reference/html5/)
* [Spring Security Reference v.5.5.8 (PDF)](https://docs.spring.io/spring-security/site/docs/5.5.x/reference/pdf/spring-security-reference.pdf)

________________________________________________________________________________________________________________________
### Часть 1. - Защита на уровне методов — аннотация @PreAuthorize (теория).

Ранее мы рассматривали авторизацию и ограничение доступа на уровне URL, т.е. когда права доступа ограничиваются к 
определенному URL (endpoint-у). Оказывается, этого бывает недостаточно. Например, наше приложение не является web, 
а защита все же необходима. Значит нужны другие варианты управления правами доступа и фильтрации данных. Рассмотрим, как 
защитить отдельные методы (любые: контроллеров, сервисов и т.д.). То есть разрешить вызов метода только пользователю с 
конкретными правами и возможно при выполнении определенных условий не связанных с правом доступа.
Рассмотрим пример.

Пусть у нас есть пользователи user и admin. А также AnimalController, который позволяет выводить список животных и 
добавлять животное. Обе операции доступны по endpoint-у "/animal". Но первая вызывается методом GET, а вторая - методом POST:

      @RestController
      public class AnimalController {
        private final List<Animal> list = new ArrayList<>();
        {
          list.add(new Animal("cat"));
          list.add(new Animal("dog"));
        }

        @GetMapping("/animal")
        public List<Animal> getAnimals() {
          return list;
        }

        @PreAuthorize("hasAuthority('ROLE_ADMIN')") //Ключевая аннотация помечает защищаемый по условию метод
        @PostMapping("/animal")
        @ResponseStatus(HttpStatus.CREATED)
        public Animal addAnimal(@RequestBody Animal animal) {
          list.add(animal);
          return animal;
        }
      }

Обоим пользователям разрешен доступ к "/animal", что прописано в конфигурации безопасности:

      @EnableWebSecurity
      @EnableGlobalMethodSecurity(prePostEnabled = true) //Еще более ключевая аннотация, позволяет работать Method Security аннотациям
      public class SecurityConfig extends WebSecurityConfigurerAdapter {
      
        @Bean
        public PasswordEncoder passwordEncoder() {
          return NoOpPasswordEncoder.getInstance();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
          auth.inMemoryAuthentication()
          .withUser("user")
          .password("user")
          .authorities("ROLE_USER")
          .and()
          .withUser("admin")
          .password("admin")
          .authorities("ROLE_ADMIN");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
          http.authorizeRequests().antMatchers("/animal/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                                  .antMatchers("/**").permitAll()
                                  .and()
                                  .httpBasic()
                                  .and()
                                  .csrf().disable();
        }
      }

В методе configure (HttpSecurity http) задается тип авторизации и доступ. Сделаем так, чтобы добавлять животных (вызывать 
метод *.addAnimal()) мог вызывать только admin, но не user. Для этого используем аннотацию @PreAuthorize - поставим ее 
непосредственно над методом, мы уже сделали это см. выше класс AnimalController. И все же выделим:

        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @PostMapping("/animal")
        @ResponseStatus(HttpStatus.CREATED)
        public Animal addAnimal(@RequestBody Animal animal) {
          list.add(animal);
          return animal;
        }

В аннотации прописано, что добавлять животных может только пользователь с правом ROLE_ADMIN. Любой другой клиент приложения
при попытке добавить животное, получит 403 ответ от сервиса (сервера).

Аннотация @PreAuthorize может защищать любые методы: сервисов, контроллеров, dao (repository) и т.д.

### @PreAuthorize (практика).

- Создадим метод читающий все контакты Client-ов из БД [*.getAllContacts()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/ContactController.java#L78) в классе [ContactController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/ContactController.java) и аннотируем его, как:

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/user-contacts")

Т.е. доступ к endpoint-у "/user-contacts" будет только у клиентов с ролью "ADMIN".

- Создадим метод [*.createLoan()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java#L70) в классе [LoansController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java), и условия в аннотации @PreAuthorize будут таковыми, что в БД
будут сохранены только кредиты с clientId соответствующие ID аутентифицированного клиента.

________________________________________________________________________________________________________________________
### Часть 2. - Защита на уровне методов — аннотация @PostAuthorize (теория).

Чтобы использовать аннотацию @PostAuthorize в вашем Spring приложении, сначала необходимо, как и в предыдущем случае 
включить Spring Security - добавить необходимые зависимости. Далее, добавить аннотацию @GlobalMethodSecurity с необходимыми
разрешениями, в файл конфигурации (т.е. аннотированный @Configuration), например:

        @EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)

Чтобы задействовать аннотацию @PostAuthorize (как и @PreAuthorize), нам просто нужно аннотировать ей нуждающийся в защите 
метод. Если посмотреть внутреннюю структуру @GlobalMethodSecurity, то видно, что параметр prePostEnabled=true, т.е. не 
требует специально его задавать, в отличие от других, если они нам нужны в работе.

@PostAuthorize - аннотация безопасности Spring, используемая для указания того, что метод следует вызывать только в том 
случае, если результат после выполнения метода соответствует определенным критериям. При использовании аннотации 
@PostAuthorize очень важно помнить, что эта аннотация позволит сначала выполнить бизнес-логику метода, и только затем 
будет оценено содержащееся в ней выражение безопасности. 

        Из-за специфической работы аннотации @PostAuthorize необходимо быть осторожным и не использовать 
        ее с методами, которые выполняют модифицирующие запросы, например, Delete User или Update User.

Одним из хороших вариантов использования аннотации @PostAuthorize будет метод, который считывает некоторую информацию 
из БД или других источников и возвращает некое значение. Например, мы можем использовать аннотацию @PostAuthorize с 
методом, который считывает данные пользователя из базы данных, а затем возвращает объект пользователя из метода.

Давайте напишем выражение безопасности, которое позволяет методу возвращать объект пользователя только в том случае, 
если userId пользователя совпадает с userId текущего вошедшего в систему основного пользователя.

И так, аннотация @PostAuthorize имеет доступ к объекту, который метод собирается вернуть. Любой объект, который наш 
метод собирается вернуть, может быть доступен в выражении безопасности через выражение "returnObject" (помним, нам 
доступен SpEL).

Ниже приведен пример класса Java, содержащего данные пользователя. При написании выражения безопасности для аннотации 
@PostAuthorize мы можем получить доступ к свойству userId класса с помощью - returnObject.userId:

        public class User {
            private String userId;
            private String firstName;
            private String lastName;
            
            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }
        }

Пример выражения безопасности на уровне метода, которое обращается к свойству userId возвращаемых объектов:

        @PostAuthorize("returnObject.userId == principal.userId")

Метод ниже возвращает объект класса User. Выражение, используемое в аннотации @PostAuthorize, будет проверено после 
выполнения метода, но метод фактически вернет значение только userId текущего вошедшего в систему пользователя, которое 
будет соответствовать userId объекта User.

        @PostAuthorize("returnObject.userId == principal.userId")
        @GetMapping(path = "/{id}", 
                    produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
        public User getUser(@PathVariable String id) {
            
            User returnValue = new User();
            UserDto userDto = userService.getUserByUserId(id);
            ModelMapper modelMapper = new ModelMapper();
            returnValue = modelMapper.map(userDto, User.class);
            
            return returnValue;
        }

Если наше приложение поддерживает роли и полномочия пользователя, мы можем прописать выражения безопасности, которые 
проверяют полномочия пользователя. Например, приведенная ниже аннотация безопасности @PostAuthorize позволит методу 
возвращать значение только в том случае, если вошедший в систему пользователь имеет роль ADMIN или является владельцем 
возвращаемого объекта.

        @PostAuthorize("hasRole('ADMIN') or returnObject.userId == principal.userId")

Обратите внимание на использование метода hasRole(). Метод hasRole() используется для проверки того, имеет ли текущий 
аутентифицированный пользователь определенную роль. В приведенном выше примере кода выражение указывает, что метод 
должен выполняться только в том случае, если аутентифицированный пользователь имеет роль «ADMIN» или если поле «userId» 
возвращаемого объекта равно «userId» текущего аутентифицированного пользователя.

Таким образом, если у пользователя есть роль «ADMIN», метод будет выполнен независимо от значения поля «userId». Однако, 
если у пользователя нет роли «ADMIN», метод будет выполнен только в том случае, если поле «userId» возвращаемого объекта 
совпадает с «userId» аутентифицированного пользователя.

### @PostAuthorize (практика).

Проверим все описанное выше для аннотации @PostAuthorize на практике. В классе [ContactController](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/ContactController.java) добавим метод:

        @PostAuthorize("#myCity == returnObject.city")
        @GetMapping("/user-contact-with-condition/{myCity}")
        public ContactReadDto getContactsIfConditionIsGood(@Param("myCity") @PathVariable("myCity") String myCity,
                                                           @AuthenticationPrincipal UserDetails userDetails) { 
            // some code 
        }

Он будет реагировать на запросы аутентифицированных клиентов, но после исполнения 'бизнес логики' из структуры 
полученного в ответ объекта мы извлечем название города (city) и сравним с переданным в аргументах (myCity). Если 
названия совпадают, то клиент (пользователь) получает доступ к результатам работы метода, нет получает отказ в доступе.

#### Несколько аннотаций безопасности для одного метода (дополнения к теории):

Мы также можем использовать несколько аннотаций безопасности в одном методе:

        @PreAuthorize("#username == authentication.principal.username")
        @PostAuthorize("returnObject.username == authentication.principal.nickName")
        public CustomUser securedLoadUserDetail(String username) {
            return userRoleRepository.loadUserByUserName(username);
        }

Таким образом, Spring проверит авторизацию как до, так и после выполнения метода *.secureLoadUserDetail().

________________________________________________________________________________________________________________________
### Часть 3. - Защита на уровне методов — аннотации @PreFilter и @PostFilter(теория).

Как и предыдущие аннотации (@PostAuthorize и @PreAuthorize) @PreFilter и @PostFilter позволяют нам задавать более точно 
правила безопасности с использованием Spring Expression Language. Однако у данных аннотаций немного другая специфика 
работы.

Аннотации @PreFilter и @PostFilter используются для фильтрации списков объектов (но не только для этого) на основе 
определяемых пользовательских правил безопасности:
- @PostFilter - Определяет правило для фильтрации списка возвращаемого аннотированным методом. Заданное в параметрах 
аннотации правило применяется к каждому элементу в возвращаемом списке. Если оцененное значение истинно, элемент будет 
сохранен в списке. В противном случае элемент не попадет в возвращаемый методом список (или будет удален из него).
- @PreFilter - Работает похожим образом, однако фильтрация применяется к списку, который передается в качестве входного 
аргумента аннотированному методу.

Обе аннотации можно использовать для методов, классов и интерфейсов. Но, ниже будет описано их использование только к 
методам. Эти аннотации не активны по умолчанию — их нужно включить (как и рассмотренные ранее) с помощью аннотации 
@EnableMethodSecurity и prePostEnabled = true:

        @Configuration
        @EnableWebSecurity
        @EnableMethodSecurity(prePostEnabled = true)
        public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
            // ...
        }

Чтобы написать правила безопасности в этих двух аннотациях, мы снова будем использовать выражения Spring-EL. Мы также 
можем использовать встроенный объект filterObject, чтобы получить ссылку на конкретный тестируемый элемент списка. Spring 
Security предоставляет множество других встроенных объектов для создания очень конкретных и точных правил.

Например, мы можем использовать @PostFilter, чтобы проверить, равно ли свойство assignee объекта Task имени текущего 
аутентифицированного пользователя:

        @PostFilter("filterObject.assignee == authentication.name")
        List<Task> findAll() {
            ...
        }

Мы использовали здесь аннотацию @PostFilter, так как хотим, чтобы метод сначала выполнялся и получил список Task-ов, и 
только затем каждый элемент списка был обработан согласно условиям фильтрации.

Таким образом, если аутентифицированным пользователем является некто Michael, окончательный список задач, возвращаемый 
методом *.findAll(), будет содержать только задачи, назначенные Michael-у, даже если в базе данных есть задачи,  
назначенные John-у, Paul-у и т.д.

Теперь давайте сделаем правило фильтрации чуть сложнее. Предположим, что пользователь является менеджером, и он, может 
видеть все задачи, независимо от того кому они назначены:

        @PostFilter("hasRole('MANAGER') or filterObject.assignee == authentication.name")
        List<Task> findAll() {
            ...
        }

Мы использовали встроенный метод hasRole, чтобы проверить, есть ли у аутентифицированного пользователя роль MANAGER. 
Если hasRole возвращает true, задача останется в финальном списке. Таким образом, если пользователь является менеджером, 
правило будет возвращать значение true для каждого элемента в списке. Таким образом, окончательный список будет содержать 
все элементы.

Теперь давайте отфильтруем список, переданный в качестве параметра методу сохранения, используя @PreFilter:

        @PreFilter("hasRole('MANAGER') or filterObject.assignee == authentication.name")
        Iterable<Task> save(Iterable<Task> entities) {
            ...
        }

Правило безопасности такое же, как и в примере с @PostFilter. Основное отличие здесь заключается в том, что элементы 
списка будут отфильтрованы до выполнения метода. Это позволит нам удалить некоторые элементы из переданного в метод 
списка, запретив их сохранение в БД.

Например, John, который не является менеджером, захочет сохранить список задач через метод *.save() описанный выше. 
Однако, некоторые из задач принадлежат другим исполнителям (Sam, Pamella и т.д.). Тогда согласно условию фильтра будут 
включены только те задачи, которые назначены исключительно John-у, остальные будут игнорироваться.

#### Особенность работы аннотаций @PreFilter, PostFilter и их производительность в фильтрации больших списков.

@PostFilter простой в использовании инструмент, но он может быть неэффективным при работе с очень большими списками, 
поскольку операция выборки будет извлекать все данные, и только затем применять фильтр. Та же ситуация может быть и
в случае с PreFilter, т.к. неизвестно, кто и что "подадут на вход префильтруемому" методу.

Представим, что у нас есть тысячи задач в БД, а мы хотим получить только пять, которые в настоящее время назначены John-у.
Если мы используем @PostFilter, операция базы данных сначала извлечет все задачи, а затем переберет их все, чтобы 
отфильтровать нужные согласно заданному правилу.

### @PostFilter и @PreFilter (практика).

- Поиграем с аннотацией @PreFilter. Поскольку данные фильтры рассчитаны на работу с коллекциями, недурно было бы узнать, 
как отправлять коллекцию в метод контроллера в теле POST запроса, данный материал кратко изложен в [JSON_and_POST_request.md](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/DOC/JSON_Collection_to_POST/JSON_and_POST_request.md). 
Что мы и применили в LoanCreateDto.java - расставили аннотации над геттерами, далее создали метод [*.saveAllMyRequestLoan()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java#L108) 
в [LoansController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java), в котором аннотация @PreFilter отсекает кредиты из переданной коллекции не принадлежащие текущему 
аутентифицированному клиенту. Далее см. по MVC цепочке как двигается запрос и формируется ответ.

- Поиграем с аннотацией @PostFilter. У нас есть таблица loans содержащая поле loan_type - условно тип кредита (на что ушли 
деньги) выберем записи со значениями из БД. Сам тип кредита передадим через параметр адресной строки. Конечно, тут бы
прекрасно отработал обычный CRUD метод с уточнениями по выбранному полю, перфоманс приложения однозначно был бы лучше - 
но мы хотим именно испробовать @PostFilter. Недостаток был описан выше - сначала метод выгребет из БД все записи и только 
затем сделает необходимые фильтрации, т.е. мы рискуем уронить сервис в случае огромного количества записей. Но аннотация
@PostFilter все же является частью системы безопасности, и как нам кажется, имеет скорее вспомогательную (доводящую до 
нужной кондиции) функцию. Ее работу мы тестируем в методе [*.getAllLoanByType()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java#L130) класса [LoansController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java).

________________________________________________________________________________________________________________________
### Часть 4. - Аннотация @EnableMethodSecurity (теория).

С помощью Spring Security мы можем настроить аутентификацию и авторизацию приложения практически для любых методов (необязательно web).
Например, если у пользователя есть аутентификация в нашем сервисе, мы можем профилировать его взаимодействие с сервисом, 
применяя ограничения к существующим методам (именно к методам).

Использование аннотации @EnableGlobalMethodSecurity было стандартом до версии 5.6, когда @EnableMethodSecurity представил 
более гибкий способ настройки авторизации для безопасности методов. Поэтому не стоит удивляться если мы увидим одну из 
этих аннотаций @EnableMethodSecurity или @EnableGlobalMethodSecurity в чьем либо проекте (они делают одну работу - позволяют 
нам использовать декларативный способ защиты отдельных методов приложения). 

@EnableMethodSecurity - функциональный интерфейс, который нам нужен наряду с @EnableWebSecurity для создания и
настройки безопасности приложения и получения авторизации метода, пример класса конфигурации ([AppSecurityConfig](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/config/security_config/AppSecurityConfig.java)):

         @EnableWebSecurity
         @EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
         @Configuration
         public class SecurityConfig {
            // security beans
         }

Все реализации безопасности методов используют MethodInterceptor, который срабатывает, когда требуется авторизация.  
В случае с классом GlobalMethodSecurityConfiguration, то он является базовой конфигурацией для включения глобальной 
безопасности методов. Spring Security поддерживает три встроенных аннотации безопасности методов:
- prePostEnabled - для аннотаций Spring @Pre/Post_Authorize (обычно true by default); 
- securedEnabled - для аннотации Spring @Secured;
- jsr250Enabled - для стандартной аннотации Java @RoleAllowed;

@EnableMethodSecurity отличается от @EnableGlobalMethodSecurity. С помощью @EnableMethodSecurity мы видим намерение 
Spring Security перейти на конфигурацию на основе bean-компонентов для типов авторизации. Все типы авторизации 
по-прежнему поддерживаются. Например, по-прежнему реализуется JSR-250. Однако нам не нужно добавлять prePostEnabled в 
аннотацию, поскольку теперь по умолчанию он равен true, остальные нужно выставлять согласно нашим потребностям:

         @EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)

________________________________________________________________________________________________________________________
### Часть 5. - Аннотация @Secured (теория). 

Аннотация @Secured используется для указания списка ролей в методе. Таким образом, пользователь может получить доступ к 
этому методу только в том случае, если у него есть хотя бы одна из указанных ролей.

Давайте определим метод *.getUsername() :

         @Secured("ROLE_VIEWER")
         public String getUsername() {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            return securityContext.getAuthentication().getName();
         }

Здесь аннотация @Secured(“ROLE_VIEWER”) определяет, что только пользователи, имеющие роль VIEWER, могут выполнять метод 
*.getUsername(). Кроме того, мы можем определить список ролей в аннотации @Secured:

         @Secured({ "ROLE_VIEWER", "ROLE_EDITOR" })
         public boolean isValidUsername(String username) {
            return userRoleRepository.isValidUsername(username);
         }

В этом случае конфигурация указывает, что если у пользователя есть полномочия VIEWER или EDITOR, этот пользователь может 
вызвать метод *.isValidUsername().

         !!! Важно. Аннотация @Secured не поддерживает Spring Expression Language (SpEL). !!!

### @Secured (практика).

Далеко ходить не будем, давайте защитим аннотацией метод [*.getAllLoanByType()](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java#L130) класса [LoansController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_10_1/src/main/java/me/oldboy/controllers/api/LoansController.java). На нем мы 
отрабатывали аннотацию @PostFilter, а теперь добавим еще и средство защиты в виде проверки права доступа "ROLE_". Пусть,
у нас, данный метод будет доступен только для клиентов с правами ADMIN. 
________________________________________________________________________________________________________________________
### Часть 6. - Аннотация @RolesAllowed (теория).

Аннотация @RolesAllowed является эквивалентом аннотации @Secured в JSR-250. По сути, мы можем использовать аннотацию 
@RolesAllowed аналогично @Secured. Таким образом, мы могли бы переопределить методы *.getUsername() и *.isValidUsername():

         @RolesAllowed("ROLE_VIEWER")
         public String getUserNameTwo() {
            //...
         }
         
         @RolesAllowed({ "ROLE_VIEWER", "ROLE_EDITOR" })
         public boolean isValidUserNameTwo(String username) {
            //...
         }

Аналогично, только пользователь с ролью VIEWER может выполнить *.getUserNameTwo(). Опять же, пользователь может вызвать 
*.isValidUserNameTwo() только в том случае, если у него есть хотя бы одна из ролей VIEWER или EDITOR.

#### Неочевидные выводы:
- Использование существующей цепочки фильтров spring-security помогает выполнять существующую + пользовательскую 
авторизацию гораздо более эффективным способом;
- Избегаем шаблонного кода;
- У нас есть полный контроль над входными данными, которые мы хотим передать в качестве аргументов в выбранные аннотации. 
- Пользовательские аннотации для уровня метода/параметра/поля дают гораздо большую гибкость и контроль над переданными 
входными данными и управлением возвращаемых данных.

#### Неочевидные для новичков проблемы в применении безопасности на уровне методов:
- По умолчанию проксирование Spring AOP используется для применения безопасности метода. Если защищенный метод A вызывается 
другим методом в том же классе, то аннотация безопасности в A полностью игнорируется. Это означает, что метод A будет 
выполняться без какой-либо проверки безопасности. То же самое относится и к частным методам.
- Spring SecurityContext привязан к потоку. По умолчанию контекст безопасности не распространяется на дочерние потоки, 
это нужно помнить.
________________________________________________________________________________________________________________________
### Key Class and Methods:

* [org.springframework.security.access.prepost](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/package-summary.html)

* [Class HttpSecurity](https://docs.spring.io/spring-security/site/docs/5.0.0.M5/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#addFilterBefore(javax.servlet.Filter,%20java.lang.Class))
* [Interface HttpSecurityBuilder](https://docs.spring.io/spring-security/site/docs/4.1.0.RC2/apidocs/org/springframework/security/config/annotation/web/HttpSecurityBuilder.html)

### Reference Documentation:

* [EnableMethodSecurity](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/method/configuration/EnableMethodSecurity.html)
* [PreFilter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PreFilter.html)
* [PostFilter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PostFilter.html)
* [PostAuthorize](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PostAuthorize.html)
* [PreAuthorize](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/prepost/PreAuthorize.html)

* [JSON Web Token (JWT)](https://www.rfc-editor.org/rfc/rfc7519.html)
* [JSON Web Token](https://en.wikipedia.org/wiki/JSON_Web_Token)

* [Spring Security Filter Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
* [CSRF](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html)
* [Cross Site Request Forgery (CSRF)](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
* [CORS Servlet Applications](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
* [CORS Reactive Applications](https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html)

* [UserDetails](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details.html)
* [UserDetailsService](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html)

* [Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/index.html)
* [AuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html)
* [JDBC Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc-datasource)
* [DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html)
* [Core Configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)
* [Username/Password Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html#publish-authentication-manager-bean)

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Reference Guide (history)](https://docs.spring.io/spring-boot/docs/)
* [Spring Security](https://spring.io/projects/spring-security)
* [Spring Security Examples](https://spring.io/projects/spring-security#samples)

### Guides:

* [Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
 
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Spring Guides](https://spring.io/guides)
* [Spring Boot Security Auto-Configuration](https://www.baeldung.com/spring-boot-security-autoconfiguration)
* [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)
* [Spring Security: Upgrading the Deprecated WebSecurityConfigurerAdapter](https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter)

### Articles (question-answer):

* [Introduction to Spring Method Security](https://www.baeldung.com/spring-security-method-security)
* [Spring @EnableMethodSecurity Annotation](https://www.baeldung.com/spring-enablemethodsecurity)

* [JSON Web Token Claims](https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-token-claims)
* [Пять простых шагов для понимания JSON Web Tokens (JWT)](https://habr.com/ru/articles/340146/)
* [JWT Security Best Practices](https://curity.io/resources/learn/jwt-best-practices/)

* [A Custom Filter in the Spring Security Filter Chain](https://www.baeldung.com/spring-security-custom-filter)
* [Путешествие к центру Spring Security](https://habr.com/ru/articles/724738/)
* [CSRF Protection in Spring Security](https://www.geeksforgeeks.org/csrf-protection-in-spring-security/)
* [A Guide to CSRF Protection in Spring Security](https://www.baeldung.com/spring-security-csrf)
* [Spring Security - CORS](https://www.geeksforgeeks.org/spring-security-cors/)
* [CORS with Spring](https://www.baeldung.com/spring-cors)
* [Spring Security – UserDetailsService and UserDetails with Example](https://www.geeksforgeeks.org/spring-security-userdetailsservice-and-userdetails-with-example/)
* [@Valid Annotation on Child Objects](https://www.baeldung.com/java-valid-annotation-child-objects)
* [Java Bean Validation Basics](https://www.baeldung.com/java-validation)
* [Javax validation on nested objects - not working](https://stackoverflow.com/questions/53999226/javax-validation-on-nested-objects-not-working)
* [Проверка данных — Java & Spring Validation](https://habr.com/ru/articles/424819/)

### Old material (repeat):

* [Spring Boot lessons part 21 - Security Starter - PART 2](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_21)
________________________________________________________________________________________________________________________