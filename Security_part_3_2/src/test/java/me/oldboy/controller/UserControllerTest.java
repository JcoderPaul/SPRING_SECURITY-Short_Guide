package me.oldboy.controller;

import me.oldboy.model.User;
import me.oldboy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserController userController;
    private List<User> expectedUserList;

    @BeforeEach
    void initList(){
        expectedUserList = new ArrayList<>(asList(new User("first", "1234", 1),
                                                  new User("second", "4321", 1)));
    }

    /* Проверяем только логику метода, без взаимодействия с системой безопасности */
    @Test
    void shouldReturnUserListGetUserListTest() {
        doReturn(expectedUserList).when(userRepository).findAll(); // Имитируем обращение к репозиторию
        int expectedListSize = expectedUserList.size(); // Узнаем размер исходной коллекции

        List<User> actualUserList = userController.getUserList(); // Обращаемся к контроллеру для получения коллекции из "репозитория"

        assertThat(actualUserList).isEqualTo(expectedUserList); // Коллекция возвращаемая "репозиторием", должна совпадать с коллекцией возвращаемой контроллером
        assertThat(actualUserList.size()).isEqualTo(expectedListSize); // Размер коллекции совпадает с расчетным

        verify(userRepository, times(1)).findAll(); // Метод репозитория вызывался один раз
    }

    /*
    Проверяем как отрабатывает Spring Security при обращении к методу с должным значением роли. Тут обращение
    репозитория идет к реально запущенной базе данных, т.е. разумнее всего было бы использовать, что-то типа
    TestContainers, но у нас метод GET, и он не изменяет состояние таблиц, поэтому на данном этапе оставим так.
    */
    @Test
    @WithMockUser(roles = {"HR"})
    void shouldReturnUserListWithAuthClientGetUserListTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/userList"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("{\"id\":1,\"username\":\"Paul\",\"password\":\"{noop}12345\",\"enabled\":1}," +
                          "{\"id\":2,\"username\":\"Sasha\",\"password\":\"{noop}54321\",\"enabled\":1}," +
                          "{\"id\":3,\"username\":\"Stasya\",\"password\":\"{noop}98765\",\"enabled\":1}");
    }
}