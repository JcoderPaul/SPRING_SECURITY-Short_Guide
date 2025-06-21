package me.oldboy.config.security_config;

import me.oldboy.filters.JwtTokenGeneratorAndAfterFilter;
import me.oldboy.filters.JwtTokenValidatorAndBeforeFilter;
import me.oldboy.filters.UserPassValidatorAndAfterLogoutFilter;
import me.oldboy.services.ClientService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

public class FilterChainConfig {
    public static SecurityFilterChain getSecurityFilterChain(HttpSecurity httpSecurity,
                                                             UserDetailsService userDetailsService,
                                                             ClientService clientService) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .addFilterAfter(new UserPassValidatorAndAfterLogoutFilter(userDetailsService, clientService), LogoutFilter.class)
                .addFilterBefore(new JwtTokenValidatorAndBeforeFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JwtTokenGeneratorAndAfterFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(urlConfig -> urlConfig
                        .requestMatchers(antMatcher("/api/regClient"),
                                antMatcher("/api/loginClient")).permitAll()
                        .requestMatchers(antMatcher("/api/myAccount"),
                                antMatcher("/api/myBalance"),
                                antMatcher("/api/myContact"),
                                antMatcher("/api/myLoans")).authenticated()
                        .requestMatchers(antMatcher("/api/myCards"),
                                antMatcher("/api/admin/**")).hasAnyAuthority("READ", "ADMIN")
                        .anyRequest().authenticated());

        return httpSecurity.build();
    }
}
