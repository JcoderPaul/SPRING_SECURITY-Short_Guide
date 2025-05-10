package me.oldboy.config.auth_provider;

import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthProvider implements AuthenticationProvider {

    @Autowired
    private ClientDetailsService clientDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userEmail = authentication.getName();
        String password = authentication.getCredentials().toString();

        SecurityClientDetails mayBeClient = (SecurityClientDetails) clientDetailsService.loadUserByUsername(userEmail);
        if (!passwordEncoder.matches(password, mayBeClient.getPassword())) {
            throw new BadCredentialsException("Bad password!");
        }

        return new UsernamePasswordAuthenticationToken(mayBeClient, password, mayBeClient.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}