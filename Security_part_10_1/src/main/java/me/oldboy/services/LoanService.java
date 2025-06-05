package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.dto.loan_dto.LoanCreateDto;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.mapper.LoanMapper;
import me.oldboy.models.client.Client;
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
    @Autowired
    private ClientService clientService;

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

    @Transactional
    public Long saveLoan(LoanCreateDto loanCreateDto) {
        Loan createNewLoan = LoanMapper.INSTANCE.mapToLoan(loanCreateDto);
        Optional<Client> mayBeClient = clientService.findById(loanCreateDto.getClientId());

        if(mayBeClient.isPresent()){
            createNewLoan.setClient(mayBeClient.get());
        }

        return loanRepository.save(createNewLoan).getLoanId();
    }

    @Transactional
    public boolean saveAllMyLoans(List<LoanCreateDto> loanCreateDto, SecurityClientDetails userDetails) {
        List<Loan> toSaveLoansList = loanCreateDto.stream()
                                                  .map(LoanMapper.INSTANCE::mapToLoan)
                                                  .toList();

        toSaveLoansList.forEach(loan -> loan.setClient(userDetails.getClient()));

        return loanRepository.saveAllMyLoans(toSaveLoansList);
    }

    public List<LoanReadDto> findAll() {
        List<Loan> allLoans = loanRepository.findAll();
        return allLoans.stream().map(LoanMapper.INSTANCE::mapToLoanReadDto).toList();
    }
}