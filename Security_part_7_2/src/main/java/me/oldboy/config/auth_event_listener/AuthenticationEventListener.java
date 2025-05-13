package me.oldboy.config.auth_event_listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationEventListener implements ApplicationListener<AbstractAuthenticationEvent> {

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent authenticationEvent) {
        if (authenticationEvent instanceof InteractiveAuthenticationSuccessEvent) {
            return;
        }
        Authentication authentication = authenticationEvent.getAuthentication();
        String auditMessage = "Login attempt with username: " +
                authentication.getName() + " " +
                authentication.getAuthorities() + " " +
                authentication.getCredentials() + " " +
                authentication.getPrincipal() + " " +
                "\t\tSuccess: " + authentication.isAuthenticated();
        System.out.println(auditMessage);
    }

}
