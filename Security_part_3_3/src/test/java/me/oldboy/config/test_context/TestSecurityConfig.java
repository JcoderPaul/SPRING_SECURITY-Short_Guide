package me.oldboy.config.test_context;

import me.oldboy.config.security.FilterChainConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ComponentScan({"me.oldboy.config.test_context",
                "me.oldboy.controllers"})
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return FilterChainConfig.getSecurityFilterChain(httpSecurity);
    }
}
