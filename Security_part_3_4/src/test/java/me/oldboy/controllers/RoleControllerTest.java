package me.oldboy.controllers;

import me.oldboy.models.PrintAuthorities;
import me.oldboy.repository.RoleRepository;
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

/* Делаем обычный Mock тест без реальной интеграции с Repository зависимостью и БД */

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock // "Заглушка"
    private RoleRepository roleRepository;
    @InjectMocks // Во что будет помещена "заглушка"
    private RoleController roleController;
    private List<PrintAuthorities> authoritiesList; // Что должна возвращать "заглушка"

    @BeforeEach // Предварительная настройка данных для тестов
    void setTests(){
        authoritiesList = new ArrayList<>(asList(new PrintAuthorities("andOne", "WRITER"),
                                                 new PrintAuthorities("andTwo", "READER")));
    }

    @Test
    void shouldReturnListSizeGetClientList() {
        /* Проверяем работу "заглушки" и возвращаемую ей коллекцию */
        when(roleRepository.findAll()).thenReturn(authoritiesList); // Возвращаем подготовленные данные в случае обращения к "заглушке"
        int listSizeFromRepository = authoritiesList.size(); // Проверяем размер возвращаемого "заглушкой" списка

        /* Тестируем основной метод */
        List<PrintAuthorities> listFromController = roleController.getClientList(); // Смотрим результат работы основного метода
        int listSizeFromController = listFromController.size(); // Получаем размер коллекции возвращенной тестируемым методом

        assertThat(listSizeFromController).isEqualTo(listSizeFromRepository); // Сравниваем исходный и тестируемый размер

        verify(roleRepository, times(1)).findAll(); // В ходе теста к методу "заглушки" обратились 1-н раз
    }
}