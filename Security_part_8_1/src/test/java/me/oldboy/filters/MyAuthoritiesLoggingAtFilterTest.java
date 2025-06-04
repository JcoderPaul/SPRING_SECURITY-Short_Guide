package me.oldboy.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith({OutputCaptureExtension.class})
class MyAuthoritiesLoggingAtFilterTest {

    @Mock
    private ServletRequest request;
    @Mock
    private ServletResponse response;
    @Mock
    private FilterChain chain;
    @InjectMocks
    private MyAuthoritiesLoggingAtFilter myAuthLoggingAtFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SneakyThrows
    void myAuthoritiesLoggingAtFilter_DoFilter_Test(CapturedOutput output) {
        myAuthLoggingAtFilter.doFilter(request, response, chain);    // Вызываем тестируемый метод

        assertThat(output).contains(" \n *** 3 - Log MyAuthoritiesLoggingAtFilter method *** \n" +
                " *** Method is in progress *** ");

        verify(chain,times(1)).doFilter(request, response);  // Проверяем, что chain.doFilter был вызван
    }
}