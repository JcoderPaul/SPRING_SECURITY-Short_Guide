package me.oldboy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityDemoAppPart2_2Test {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void contextLoads() {
        assertNotNull(webApplicationContext);
    }
  
}