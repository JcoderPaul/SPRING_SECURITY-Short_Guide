package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.exception.AccountServiceException;
import me.oldboy.mapper.AccountMapper;
import me.oldboy.models.money.Account;
import me.oldboy.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public AccountReadDto readAccountByClientId(Long clientId){
        Optional<Account> mayBeAccount = accountRepository.findByClientId(clientId);
        if (mayBeAccount.isPresent()) {
            return AccountMapper.INSTANCE.mapToAccountReadDto(mayBeAccount.get());
        } else {
            throw new AccountServiceException("Client with ID " + clientId + " have no account!");
        }
    }
}