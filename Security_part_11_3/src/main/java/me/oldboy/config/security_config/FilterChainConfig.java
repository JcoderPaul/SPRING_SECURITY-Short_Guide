package me.oldboy.config.security_config;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

public class FilterChainConfig {
    public static SecurityFilterChain getSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(urlConfig -> urlConfig
                        .requestMatchers(antMatcher("/api/regClient")).permitAll()
                        .requestMatchers(antMatcher("/api/myAccount"),
                                antMatcher("/api/myBalance"),
                                antMatcher("/api/myContact"),
                                antMatcher("/api/myLoans")).authenticated()
                        .requestMatchers(antMatcher("/api/myCards"),
                                antMatcher("/api/admin/**")).hasRole("ADMIN")
                        .anyRequest().authenticated());

        return httpSecurity.build();
    }
}
