package me.oldboy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan("me.oldboy")
public class SecurityDemoAppPart_11_1 {
    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoAppPart_11_1.class, args);
    }
}
