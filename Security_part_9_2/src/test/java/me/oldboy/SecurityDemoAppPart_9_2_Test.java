package me.oldboy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SecurityDemoAppPart_9_2_Test {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void mainContextLoaderTest(){
        assertNotNull(webApplicationContext);
    }
}