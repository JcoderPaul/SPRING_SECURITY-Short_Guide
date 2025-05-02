package me.oldboy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
@ComponentScans({ @ComponentScan("me.oldboy.config"),
		          @ComponentScan("me.oldboy.controller") })
public class SpringSecurityPart2App {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityPart2App.class, args);
	}

}
