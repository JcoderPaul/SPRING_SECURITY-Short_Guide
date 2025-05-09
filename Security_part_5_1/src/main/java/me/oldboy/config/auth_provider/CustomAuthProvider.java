package me.oldboy.config.auth_provider;

import me.oldboy.models.Client;
import me.oldboy.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomAuthProvider implements AuthenticationProvider {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userEmail = authentication.getName();
        String password = authentication.getCredentials().toString();

        Optional<Client> mayBeClient = clientRepository.findByEmail(userEmail);
        if (mayBeClient.isEmpty()) {
            throw new BadCredentialsException("User with email/name " + userEmail + " not found!");
        }
        if (!passwordEncoder.matches(password, mayBeClient.get().getPass())) {
            throw new BadCredentialsException("Bad password!");
        }

        UserDetails principal = User.builder()
                .username(mayBeClient.get().getEmail())
                .password(mayBeClient.get().getPass())
                .roles(mayBeClient.get().getRole().name())
                .authorities(mayBeClient.get().getRole().name())
                .build();

        return new UsernamePasswordAuthenticationToken(principal, password, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}