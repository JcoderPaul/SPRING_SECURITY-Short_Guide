package me.oldboy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("me.oldboy")
public class SpringSecurityBasicApp {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityBasicApp.class, args);
	}

}
