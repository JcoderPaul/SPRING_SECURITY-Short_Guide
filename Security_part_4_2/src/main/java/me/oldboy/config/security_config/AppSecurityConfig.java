package me.oldboy.config.security_config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan({
	"me.oldboy.controllers"
	,"me.oldboy.services"
	,"me.oldboy.repository"
	,"me.oldboy.config.data_source"
	,"me.oldboy.config.security_details"
	,"me.oldboy.config.auth_config"
})
public class AppSecurityConfig {

	/*
	И снова, без явного внедрения ClientDetailsService зависимости и bean-a AuthenticationManager наше
	приложение будет работать, т.е. комментируем их и все крутиться-вертится, как будто они есть. И это
	так, у нас есть DaoAuthenticationProvider, который в текущей реализации используется по умолчанию.
	Все как в старом фильме, даже если мы не видим суслика - он все равно где-то есть! И самое главное,
	нужно помнить, что любое Spring Web приложение будь то Boot или nonBoot нуждается в контейнере
	сервлетов, который тоже выполняет некую работу будь он внешний или интегрированный.
	*/

	@Bean
	@SneakyThrows
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
		return FilterChainConfig.getSecurityFilterChain(httpSecurity);
	}
}