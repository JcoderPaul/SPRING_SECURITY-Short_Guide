package me.oldboy.integration;

import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@IT
public abstract class TestContainerInit {
    /*
        Получаем тест контейнер, в параметрах передаем тэг нашей Docker БД. Обычно, значений по-умолчанию
        самого PostgreSQLContainer хватает, но их легко можно задать используя его же методы.
    */
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:13");

    /*
        Запускаем тест контейнер. Хотя аннотация говорит о запуске контейнера перед каждым тестом, но, если
        зайти в метод *.start(), то можно заметить, как ID контейнера определяется единожды для всех тестов.
    */
    @BeforeAll
    static void runContainer() {
        container.start();
    }

    /* Получаем свойства сгенерированные динамически при запуске тестов, нам нужен был URL, вот его мы и извлекаем. */
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        /*
            DynamicPropertyRegistry - реестр, используемый с методами @DynamicPropertySource,
            чтобы они могли добавлять в среду свойства, имеющие динамически разрешаемые значения.
        */
        registry.add("spring.datasource.url", () -> container.getJdbcUrl());
    }
}