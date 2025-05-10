package me.oldboy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan("me.oldboy")
public class SecurityDemoAppPart_5_2 {
    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoAppPart_5_2.class, args);
    }
}
