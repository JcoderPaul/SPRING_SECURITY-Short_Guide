package me.oldboy.controllers.webui;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.mapper.ClientMapper;
import me.oldboy.models.client.Client;
import me.oldboy.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/webui")
public class WebClientController {

    @Autowired
    private ClientService clientService;
    @Autowired
    private ContactService contactService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private CardService cardService;
    @Autowired
    private LoanService loanService;

    @GetMapping("/account")
    public String clientAccount(Model model, @CurrentSecurityContext SecurityContextHolder securityContextHolder){
        Authentication authentication = securityContextHolder.getContext().getAuthentication();
        UserDetailsDetector userDetailsDetector = new UserDetailsDetector();

        if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
            long clientId = userDetailsDetector.getClientId();
            long accountNumber = userDetailsDetector.getAccountNumber();
            Client currentClient = userDetailsDetector.getCurrentClient();

            model.addAttribute("client", ClientMapper.INSTANCE.mapToClientReadDto(currentClient));
            model.addAttribute("account_number", accountNumber);

            Optional<ContactReadDto> mayBeContact = contactService.readContact(clientId);
            if(mayBeContact.isPresent()){
                model.addAttribute("contact", mayBeContact.get());
            } else {
                model.addAttribute("contact", new ContactReadDto());
            }
        } else {
            model.addAttribute("client", new Client());
        }

        return "main_items/account.html";
    }

    @GetMapping("/contacts")
    public String clientContact(Model model, @CurrentSecurityContext SecurityContextHolder securityContextHolder){
        Authentication authentication = securityContextHolder.getContext().getAuthentication();
        UserDetailsDetector userDetailsDetector = new UserDetailsDetector();

        if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
            long clientId = userDetailsDetector.getClientId();
            String clientEmail = userDetailsDetector.getClientEmail();

            Optional<ContactReadDto> mayBeContact = contactService.readContact(clientId);
            if(mayBeContact.isPresent()){
                model.addAttribute("contact", mayBeContact.get());
                model.addAttribute("email", clientEmail);
            } else {
                model.addAttribute("contact", new ContactReadDto());
            }
        }

        return "main_items/contacts.html";
    }

    @GetMapping("/balance")
    public String clientBalance(Model model, @CurrentSecurityContext SecurityContextHolder securityContextHolder){
        Authentication authentication = securityContextHolder.getContext().getAuthentication();
        UserDetailsDetector userDetailsDetector = new UserDetailsDetector();

        if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
            long clientId = userDetailsDetector.getClientId();
            long accountNumber = userDetailsDetector.getAccountNumber();

            model.addAttribute("account_number", accountNumber);

            List<TransactionReadDto> mayBeTransactions = balanceService.readAllTransactionByClientId(clientId);
            if(mayBeTransactions.size() != 0){
                model.addAttribute("transactions", mayBeTransactions);
            } else {
                model.addAttribute("transactions", List.of());
            }
        }

        return "main_items/balance.html";
    }

    @GetMapping("/cards")
    public String clientCards(Model model, @CurrentSecurityContext SecurityContextHolder securityContextHolder){
        Authentication authentication = securityContextHolder.getContext().getAuthentication();
        UserDetailsDetector userDetailsDetector = new UserDetailsDetector();

        if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)) {
            long clientId = userDetailsDetector.getClientId();
            long accountNumber = userDetailsDetector.getAccountNumber();

            model.addAttribute("account_number", accountNumber);

            List<CardReadDto> mayBeCards = cardService.findAllCardsByClientId(clientId);
            if (mayBeCards.size() != 0) {
                model.addAttribute("cards", mayBeCards);
            } else {
                model.addAttribute("cards", List.of());
            }
        }

        return "main_items/cards.html";
    }

    @GetMapping("/loans")
    public String clientLoans(Model model, @CurrentSecurityContext SecurityContextHolder securityContextHolder){
        Authentication authentication = securityContextHolder.getContext().getAuthentication();
        UserDetailsDetector userDetailsDetector = new UserDetailsDetector();

        if(userDetailsDetector.isUserDetailsNotNull(clientService, authentication)){
            long clientId = userDetailsDetector.getClientId();
            long accountNumber = userDetailsDetector.getAccountNumber();

            model.addAttribute("account_number", accountNumber);

            List<LoanReadDto> mayBeLoans = loanService.readAllLoansByUserId(clientId);
            if(mayBeLoans.size() != 0){
                model.addAttribute("loans", mayBeLoans);
            } else {
                model.addAttribute("loans", List.of());
            }
        }

        return "main_items/loans.html";
    }

    private class UserDetailsDetector{
        private Long clientId;
        private Long accountNumber;
        private String clientEmail;
        private Optional<Client> mayBeClient;

        protected boolean isUserDetailsNotNull(ClientService clientService, Authentication authentication){
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

        protected String getClientEmail(){
            return clientEmail;
        }

        protected Long getClientId(){
            return clientId;
        }

        protected Long getAccountNumber(){
            return accountNumber;
        }

        protected Client getCurrentClient(){
            return mayBeClient.get();
        }
    }
}
