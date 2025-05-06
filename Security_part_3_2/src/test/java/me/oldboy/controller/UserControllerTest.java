package me.oldboy.controller;

import me.oldboy.model.User;
import me.oldboy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

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

    @Test
    void shouldReturnUserListWithAuthClientGetUserListTest() {
        doReturn(expectedUserList).when(userRepository).findAll(); // Имитируем обращение к репозиторию
        int expectedListSize = expectedUserList.size(); // Узнаем размер исходной коллекции

        List<User> actualUserList = userController.getUserList(); // Обращаемся к контроллеру для получения коллекции из "репозитория"

        assertThat(actualUserList).isEqualTo(expectedUserList); // Коллекция возвращаемая "репозиторием", должна совпадать с коллекцией возвращаемой контроллером
        assertThat(actualUserList.size()).isEqualTo(expectedListSize); // Размер коллекции совпадает с расчетным

        verify(userRepository, times(1)).findAll(); // Метод репозитория вызывался один раз
    }
}