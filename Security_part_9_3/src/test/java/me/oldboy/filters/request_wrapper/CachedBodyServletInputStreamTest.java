package me.oldboy.filters.request_wrapper;

import me.oldboy.filters.request_wrapper.CachedBodyServletInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CachedBodyServletInputStreamTest {

    private byte[] testData;
    private CachedBodyServletInputStream cachedBodyStream;

    @BeforeEach
    void setUp() {
        testData = "This is not empty test cached body!".getBytes();
        cachedBodyStream = new CachedBodyServletInputStream(testData);
    }

    @Test
    void readMethod_Test() throws IOException {
        /* Тестируем метод read(), читаем по одному байту и сравниваем с исходными данными */
        for (byte b : testData) {
            assertThat((int) b).isEqualTo(cachedBodyStream.read());
            /* Используем явное привидение byte к int, так как read() возвращает int */
        }
        /* После того как все байты прочитаны, read() должен возвращать -1 */
        assertThat(-1).isEqualTo(cachedBodyStream.read());
    }

    /* Тестируем isFinished() когда входной поток не прочитан */
    @Test
    void isFinishedMethod_WhenNotFinished_Test() throws IOException {
        assertThat(cachedBodyStream.isFinished()).isFalse();

        /* Прочитаем несколько байтов, но не до конца */
        cachedBodyStream.read();
        assertThat(cachedBodyStream.isFinished()).isFalse();
    }

    /* Тестируем isFinished() когда поток прочитан до конца */
    @Test
    void isFinishedMethod_WhenFinished_Test() throws IOException {
        while (cachedBodyStream.read() != -1) {
            /* Цикл не закончится пока не будет прочитан весь входной поток */
        }
        assertThat(cachedBodyStream.isFinished()).isTrue();
    }

    /* Тестируем isReady() - он всегда возвращает true */
    @Test
    void isReadyMethod_Test() {
        assertThat(cachedBodyStream.isReady()).isTrue();
    }

    /* Тестируем с пустым массивом байтов */
    @Test
    void testEmptyCachedBody() throws IOException {
        cachedBodyStream = new CachedBodyServletInputStream(new byte[0]);
        assertAll(
                () -> assertThat(-1).isEqualTo(cachedBodyStream.read()),
                () -> assertThat(cachedBodyStream.isFinished()).isTrue(),
                () -> assertThat(cachedBodyStream.isReady()).isTrue()
        );
    }
}