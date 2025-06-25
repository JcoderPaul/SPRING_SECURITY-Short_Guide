package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.mapper.CardMapper;
import me.oldboy.models.money.Card;
import me.oldboy.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public List<CardReadDto> findAllCardsByClientId(Long clientId){
        List<CardReadDto> dtoList = List.of();
        Optional<List<Card>> mayBeList = cardRepository.findByClientId(clientId);
        if (mayBeList.isPresent()) {
            dtoList = mayBeList.get()
                    .stream()
                    .map(card -> CardMapper.INSTANCE.mapToTransactionReadDto(card))
                    .collect(Collectors.toList());
        }
        return dtoList;
    }
}
