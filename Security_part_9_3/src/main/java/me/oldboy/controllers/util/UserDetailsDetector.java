package me.oldboy.controllers.util;

import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public class UserDetailsDetector {
    private Long clientId;
    private Long accountNumber;
    private String clientEmail;
    private Optional<Client> mayBeClient;

    public boolean isUserDetailsNotNull(ClientService clientService, Authentication authentication){
        if(authentication != null && clientService != null){
            clientEmail = authentication.getName();
            mayBeClient = clientService.findByEmail(clientEmail);

            if(mayBeClient.isPresent()){
                clientId = mayBeClient.get().getId();
                accountNumber = mayBeClient.get().getAccount().getAccountNumber();
            } else {
                clientId = null;
                accountNumber = null;
            }

            return true;
        } else {
            return false;
        }
    }

    public String getClientEmail(){
        return clientEmail;
    }

    public Long getClientId(){
        return clientId;
    }

    public Long getAccountNumber(){
        return accountNumber;
    }

    public Client getCurrentClient(){
        return mayBeClient.get();
    }
}
