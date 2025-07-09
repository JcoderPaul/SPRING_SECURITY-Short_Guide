package me.oldboy.config.security_config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

public class FilterChainConfig {
    public static SecurityFilterChain getSecurityFilterChain(HttpSecurity httpSecurity,
                                                             JwtDecoder jwtDecoder,
                                                             JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                                .decoder(jwtDecoder)))
                .authorizeHttpRequests(urlConfig -> urlConfig
                        .requestMatchers("/api/regClient").permitAll()
                        .requestMatchers("/api/myAccount",
                                "/api/myBalance",
                                "/api/myContact",
                                "/api/myLoans").authenticated()
                        .requestMatchers("/api/myCards",
                                "/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated());

        return httpSecurity.build();
    }
}
