package me.oldboy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
/*
Обычно мы применяем @ComponentScan("me.oldboy"), на случай если проект простой, и собран "в кучу".
Но мы можем указать нашему приложению где можно еще искать потенциальные компоненты bean-ы.
*/
@ComponentScans({ @ComponentScan("me.oldboy.controller"),
		          @ComponentScan("me.oldboy.config")})
@EnableJpaRepositories("me.oldboy.repository")
public class SimpleBankApp_3_2 {

	public static void main(String[] args) {
		SpringApplication.run(SimpleBankApp_3_2.class, args);
	}

}
