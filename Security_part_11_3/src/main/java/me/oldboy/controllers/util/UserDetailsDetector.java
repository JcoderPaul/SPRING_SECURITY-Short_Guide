package me.oldboy.controllers.util;

import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDetailsDetector {
    public Optional<Client> getClientFromBase(ClientService clientService, Authentication authentication){
        String clientEmail;
        Optional<Client> mayBeClient = Optional.empty();

        if(authentication != null && clientService != null){
            clientEmail = authentication.getName();
            mayBeClient = clientService.findByEmail(clientEmail);
        }

        return mayBeClient;
    }
}
