package me.oldboy.unit.services;

import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.models.money.Card;
import me.oldboy.repository.CardRepository;
import me.oldboy.services.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CardServiceTest {

    @Mock
    private CardRepository mockCardRepository;
    @InjectMocks
    private CardService cardService;

    private List<Card> testCardList;
    private Card testCard;
    private Long testId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testId = 1L;

        testCard = Card.builder()
                .cardId(testId)
                .amountUsed(1000)
                .availableAmount(800)
                .cardNumber("1234321234")
                .build();

        testCardList = List.of(testCard, new Card(), new Card());
    }

    @Test
    void shouldReturnEqualListSize_findAllCardsByClientId_Test() {
        when(mockCardRepository.findByClientId(testId)).thenReturn(Optional.of(testCardList));

        var currentSize = testCardList.size();
        var expectedSize = cardService.findAllCardsByClientId(testId).size();

        assertThat(currentSize).isEqualTo(expectedSize);
    }

    @Test
    void shouldReturnEmptyList_FindAllCardsByClientId_Test() {
        when(mockCardRepository.findByClientId(testId)).thenReturn(Optional.empty());

        var expectedSize = cardService.findAllCardsByClientId(testId).size();

        assertThat(0).isEqualTo(expectedSize);
    }

    @Test
    void shouldReturnCardReadDtoListElement_FindAllCardsByClientId_Test() {
        when(mockCardRepository.findByClientId(testId)).thenReturn(Optional.of(testCardList));

        var isTrueOrFalse = cardService.findAllCardsByClientId(testId).get(0) instanceof CardReadDto;

        assertThat(isTrueOrFalse).isTrue();
    }
}