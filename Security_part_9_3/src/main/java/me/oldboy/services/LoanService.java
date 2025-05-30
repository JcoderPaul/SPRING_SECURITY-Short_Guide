package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.mapper.LoanMapper;
import me.oldboy.models.money.Loan;
import me.oldboy.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    /* Работает общая аннотация над классом @Transactional, тут мы только читаем */
    public List<LoanReadDto> readAllLoansByUserId(Long clientId) {
        List<LoanReadDto> emptyList = List.of();
        Optional<List<Loan>> mayBeLoans = loanRepository.findAllByClientId(clientId);
        if (mayBeLoans.isEmpty()) {
            return emptyList;
        } else if (mayBeLoans.get().size() == 0) {
            return emptyList;
        } else {
            return mayBeLoans.get()
                    .stream()
                    .map(LoanMapper.INSTANCE::mapToLoanReadDto)
                    .collect(Collectors.toList());
        }
    }
}