package me.oldboy.filters.request_wrapper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import me.oldboy.filters.request_wrapper.CachedBodyHttpServletRequest;
import me.oldboy.filters.request_wrapper.CachedBodyServletInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/*
Тут мы будем тестировать нашу кэш-связку в комплексе - "почти интеграционное тестирование":
- CachedBodyHttpServletRequest;
- CachedBodyServletInputStream;
*/
class CachedBodyHttpServletRequestTest {

    @Mock
    private HttpServletRequest mockRequest;
    private String testRequestBodyContent, testEmptyRequestBodyContent;
    private CachedBodyServletInputStream cachedNotEmptyBodyServletInputStream, cachedEmptyBodyServletInputStream;
    private byte[] cachedNotEmptyBody, cachedEmptyBody, cachedBigBody;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testRequestBodyContent = "This is not empty test request body.";    // Мы будем тестировать не пустое тело запроса
        testEmptyRequestBodyContent = "";   // Так же будем тестировать и пустое тело запроса

        cachedNotEmptyBody = testRequestBodyContent.getBytes(StandardCharsets.UTF_8);   // Преобразуем строку в массив байт
        /* Вызываем конструктор нашего вспомогательного класса наследника ServletInputStream */
        cachedNotEmptyBodyServletInputStream = new CachedBodyServletInputStream(cachedNotEmptyBody);

        cachedEmptyBody = testEmptyRequestBodyContent.getBytes(StandardCharsets.UTF_8);
        cachedEmptyBodyServletInputStream = new CachedBodyServletInputStream(cachedEmptyBody);
    }

    @Test
    void constructorShouldCacheRequestBody_Test() throws IOException {
        /* "Мокаем" результат вызываемого метода */
        when(mockRequest.getInputStream()).thenReturn(cachedNotEmptyBodyServletInputStream);

        /* Создаем конструктор и передаем в него "заглушку" запроса */
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(mockRequest);

        /* Верифицируем число вызовов метода *.getInputStream() - вызван один раз во время конструирования */
        verify(mockRequest, times(1)).getInputStream();

        /* Проверим, что кэшированное "тело" запроса соответствует исходному */
        ServletInputStream cachedServletInputStream = cachedRequest.getInputStream();
        assertThat(cachedServletInputStream).isNotNull();

        String retrievedBody = new String(StreamUtils.copyToByteArray(cachedServletInputStream), StandardCharsets.UTF_8);
        assertThat(testRequestBodyContent).isEqualTo(retrievedBody);
    }

    @Test
    void getInputStreamShouldReturnCachedBodyMultipleTimes_Test() throws IOException {
        /* Определяем, что будет на выходе "замоканного" метода и конструируем тестируемый класс */
        when(mockRequest.getInputStream()).thenReturn(cachedNotEmptyBodyServletInputStream);
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(mockRequest);

        /* Два раза вызываем "входной поток" на экземпляре нашего "кэшера" */
        ServletInputStream firstStream = cachedRequest.getInputStream();
        ServletInputStream secondStream = cachedRequest.getInputStream();

        /* Проверяем состояние - входные потоки содержат данные */
        assertThat(firstStream).isNotNull();
        assertThat(secondStream).isNotNull();

        /* Проверяем идентичность данных - одно и то же cachedBody */
        String firstBody = new String(StreamUtils.copyToByteArray(firstStream), StandardCharsets.UTF_8);
        String secondBody = new String(StreamUtils.copyToByteArray(secondStream), StandardCharsets.UTF_8);

        assertThat(testRequestBodyContent).isEqualTo(firstBody);
        assertThat(testRequestBodyContent).isEqualTo(secondBody);

        /*
        Если взглянуть на конструктор тестируемого класса, то видно, что мы единожды извлекали входной поток из запроса:
            public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
                super(request);
                InputStream requestInputStream = request.getInputStream();
                this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
            }
        проверяем это.
        */
        verify(mockRequest, times(1)).getInputStream();
    }

    /* Если тело запроса в первых двух тестах было не пустым, то тут мы проверяем "пустое тело" */
    @Test
    void constructorShouldHandleEmptyBody_Test() throws IOException {
        /* "Мокаем" кэш пустого тела запроса */
        when(mockRequest.getInputStream()).thenReturn(cachedEmptyBodyServletInputStream);

        /* Конструируем тестируемый класс */
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(mockRequest);

        /* Получаем кэш в виде входного потока, и преобразуем в строку */
        ServletInputStream cachedServletInputStream = cachedRequest.getInputStream();
        String retrievedBody = new String(StreamUtils.copyToByteArray(cachedServletInputStream), StandardCharsets.UTF_8);

        assertThat(testEmptyRequestBodyContent).isEqualTo(retrievedBody);
    }

    @Test
    void constructorShouldHandleLargeBody_Test() throws IOException {
        /* Сформируем тестовые данные прямо тут */
        StringBuilder largeBodyBuilder = new StringBuilder();
        for (int i = 0; i < 10000; i++) { // Большое тело запроса +/- 10Kb
            largeBodyBuilder.append("a");
        }

        /* Переназначим переменные */
        testRequestBodyContent = largeBodyBuilder.toString();
        cachedBigBody = testRequestBodyContent.getBytes(StandardCharsets.UTF_8);
        cachedNotEmptyBodyServletInputStream = new CachedBodyServletInputStream(cachedBigBody);

        when(mockRequest.getInputStream()).thenReturn(cachedNotEmptyBodyServletInputStream);

        /* Снова вызываем конструктор тестируемого класса */
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(mockRequest);

        /* Проверяем результат */
        ServletInputStream cachedServletInputStream = cachedRequest.getInputStream();
        String retrievedBody = new String(StreamUtils.copyToByteArray(cachedServletInputStream), StandardCharsets.UTF_8);

        assertThat(testRequestBodyContent).isEqualTo(retrievedBody);
    }
}