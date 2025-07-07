package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.mapper.TransactionMapper;
import me.oldboy.models.money.Transaction;
import me.oldboy.repository.BalanceRepository;
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
public class BalanceService {

    @Autowired
    private BalanceRepository balanceRepository;

    public List<TransactionReadDto> readAllTransactionByClientId(Long clientId){
        List<TransactionReadDto> dtoList = List.of();
        Optional<List<Transaction>> mayBeList = balanceRepository.findByClientId(clientId);
        if (mayBeList.isPresent()) {
            dtoList = mayBeList.get()
                    .stream()
                    .map(transactions -> TransactionMapper.INSTANCE.mapToTransactionReadDto(transactions))
                    .collect(Collectors.toList());
        }
        return dtoList;
    }
}
