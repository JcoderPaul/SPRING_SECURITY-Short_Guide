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

/* Просто "мокаем" зависимости и тестируем логику без привязки к БД и контексту приложения */

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private RoleController roleController;

    private List<PrintAuthorities> printAuthoritiesList;

    @BeforeEach
    void setUpTest() {
        printAuthoritiesList = new ArrayList<>(asList(new PrintAuthorities("mr.First","HR"),
                                                      new PrintAuthorities("mr.Second","READER")));
    }

    @Test
    void shouldReturnNotEmptyListGetClientListTest() {
        when(roleRepository.findAll()).thenReturn(printAuthoritiesList);
        int testListSize = printAuthoritiesList.size();

        List<PrintAuthorities> expectedList = roleController.getClientList();
        int expectedListSize = expectedList.size();

        assertThat(testListSize).isEqualTo(expectedListSize);
        assertThat(printAuthoritiesList).isEqualTo(expectedList);

        verify(roleRepository, times(1)).findAll();
    }
}