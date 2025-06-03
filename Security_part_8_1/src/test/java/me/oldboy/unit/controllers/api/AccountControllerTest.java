package me.oldboy.unit.controllers.api;

import lombok.SneakyThrows;
import me.oldboy.controllers.api.AccountController;
import me.oldboy.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
Аннотация @WebMvcTest используется для тестирования контроллеров Spring MVC. Она сканирует все компоненты, связанные
с MVC, и не включает обычные классы @Component.

Некоторые особенности работы аннотации:
    - Загрузка только веб-слоя. Аннотация загружает контроллеры, фильтры и MVC-инфраструктуру, заменяя остальную часть
      приложения на mock-объекты.
    - Имитация реальных HTTP-запросов. С помощью MockMvc имитируются GET, POST, PUT, DELETE-запросы. Не вызываются методы
      напрямую, а только обращается к эндпоинтам, как это делает клиентское приложение.
    - Проверка различных параметров. Можно проверить, например, маппинг URL, тело запроса, тело ответа, коды статуса и
      заголовки.

Важный момент: mock-объекты для других частей приложения не создаются автоматически. Если контроллер зависит от этих
объектов, тест упадёт с ошибкой, потому что Spring не смог создать контроллер. Чтобы всё заработало, мы должны сами
имитировать все зависимости контроллера, см. ниже.
*/
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    /*
    MockMvc - это часть фреймворка Spring MVC, предназначенная для тестирования веб-приложений. Он позволяет
    имитировать запросы и ответы HTTP, что дает возможность проверять функциональность контроллеров, не запуская
    реальный HTTP-сервер.

    Аннотация @Autowired в Spring Framework используется для автоматического связывания компонентов bean-a между
    собой. Она позволяет автоматически настраивать свойства bean-a и методы, упрощая тем самым процесс инъекции
    зависимостей.
    */
    @Autowired
    private MockMvc mockMvc;
    /*
    @MockBean используется для создания Mockito-имитаций (или фиктивных объектов) для bean-ов внутри Spring Boot
    ApplicationContext. Она позволяет заменить существующий бин или добавить новый бин в контекст, который будет
    использован для тестирования.
    */
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    @WithMockUser   // Имитируем аутентифицированного пользователя
    void shouldReturnOk_WithAuthClient_GetAccountDetailsTest(){
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().isOk())
                .andExpect(content().string("Here are the account details from the DB"))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    /* Тут мы не стали делить цепочку фильтров, как ранее и теперь получаем несколько другой результат, хоть и ожидаемый */
    @Test
    @SneakyThrows
    void shouldReturn_3xx_WithoutAuthClient_GetAccountDetailsTest(){
        mockMvc.perform(get("/api/myAccount"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/webui/login"))
                .andExpect(content().string(""));
    }
}