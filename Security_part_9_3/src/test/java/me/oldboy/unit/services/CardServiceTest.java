package me.oldboy.unit.services;

import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.models.money.Card;
import me.oldboy.repository.CardRepository;
import me.oldboy.services.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository mockCardRepository;
    @InjectMocks
    private CardService cardService;

    private Card testCard;
    private CardReadDto testCardReadDto;
    private Long testId;
    private List<Card> testCardList;
    private List<CardReadDto> testCardDtoList;
    private String cardNumber, cardType;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testId = 1L;
        cardNumber = "1234xxxx4321";
        cardType = "Credit";
        testCard = Card.builder()
                .cardId(testId)
                .cardNumber(cardNumber)
                .cardType(cardType)
                .build();
        testCardReadDto = CardReadDto.builder()
                .cardNumber(cardNumber)
                .cardType(cardType)
                .build();

        testCardList = List.of(testCard);
        testCardDtoList = List.of(testCardReadDto);
    }

    @Test
    void shouldReturnEqList_FindAllCardsByClientId_Test() {
        when(mockCardRepository.findByClientId(testId)).thenReturn(Optional.of(testCardList));

        assertThat(cardService.findAllCardsByClientId(testId)).isEqualTo(testCardDtoList);

        verify(mockCardRepository, times(1)).findByClientId(anyLong());
    }

    @Test
    void shouldReturnEmptyList_FindAllCardsByClientId_Test() {
        when(mockCardRepository.findByClientId(testId)).thenReturn(Optional.empty());

        assertThat(cardService.findAllCardsByClientId(testId)).isEqualTo(List.of());

        verify(mockCardRepository, times(1)).findByClientId(anyLong());
    }
}