package me.oldboy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("me.oldboy")
public class SecurityDemoAppPart2_2 {
    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoAppPart2_2.class, args);
    }
}
