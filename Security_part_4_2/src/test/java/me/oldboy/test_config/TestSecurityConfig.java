package me.oldboy.test_config;

import me.oldboy.config.security_config.FilterChainConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ComponentScan({
    "me.oldboy.controllers"
    ,"me.oldboy.services"
    ,"me.oldboy.repository"
    ,"me.oldboy.config.security_details"
    ,"me.oldboy.config.auth_config"
})
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return FilterChainConfig.getSecurityFilterChain(httpSecurity);
    }
}
