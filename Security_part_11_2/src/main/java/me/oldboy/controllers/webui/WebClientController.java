package me.oldboy.controllers.webui;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.controllers.util.UserDetailsDetector;
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
@RequestMapping("/webui")
@AllArgsConstructor
@NoArgsConstructor
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
    @Autowired
    private UserDetailsDetector userDetailsDetector;

    @GetMapping("/account")
    public String clientAccount(Model model, @CurrentSecurityContext SecurityContextHolder securityContextHolder){
        Authentication authentication = securityContextHolder.getContext().getAuthentication();
        Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService,authentication);

        if(mayBeClient.isPresent()) {
            long clientId = mayBeClient.get().getId();
            long accountNumber = mayBeClient.get().getAccount().getAccountNumber();
            Client currentClient = mayBeClient.get().getAccount().getClient();

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
        Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService,authentication);

        if(mayBeClient.isPresent()) {
            long clientId = mayBeClient.get().getId();
            String clientEmail = mayBeClient.get().getEmail();

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
        Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService,authentication);

        if(mayBeClient.isPresent()) {
            long clientId = mayBeClient.get().getId();
            long accountNumber = mayBeClient.get().getAccount().getAccountNumber();

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
        Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService,authentication);

        if(mayBeClient.isPresent()) {
            long clientId = mayBeClient.get().getId();
            long accountNumber = mayBeClient.get().getAccount().getAccountNumber();

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
        Optional<Client> mayBeClient = userDetailsDetector.getClientFromBase(clientService,authentication);

        if(mayBeClient.isPresent()){
            long clientId = mayBeClient.get().getId();
            long accountNumber = mayBeClient.get().getAccount().getAccountNumber();

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
}
