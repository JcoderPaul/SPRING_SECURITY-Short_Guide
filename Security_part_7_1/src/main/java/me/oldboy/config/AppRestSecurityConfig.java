package me.oldboy.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan("me.oldboy")
@Order(5)
public class AppRestSecurityConfig {

    @Bean
    @SneakyThrows
    public SecurityFilterChain restFilterChain(HttpSecurity httpSecurity) {
        httpSecurity
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(urlConfig ->	urlConfig.
                        requestMatchers(antMatcher("/api/notices"),
                                antMatcher("/api/contact"))
                        .permitAll()
                        .requestMatchers(antMatcher("/api/myAccount"),
                                antMatcher("/api/myBalance"),
                                antMatcher("/api/myLoans"),
                                antMatcher("/api/myCards"))
                        .authenticated()
                        .requestMatchers(antMatcher("/api/admin/**"))
                        .hasAuthority("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return httpSecurity.build();
    }
}