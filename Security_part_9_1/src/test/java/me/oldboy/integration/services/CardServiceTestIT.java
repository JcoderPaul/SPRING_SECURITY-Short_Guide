package me.oldboy.integration.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.services.CardService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class CardServiceTestIT extends IntegrationTestBase {

    private final CardService cardService;

    private Long clientId;

    @Test
    void shouldReturnDtoList_FindAllCardsByClientId_Test() {
        clientId = 1L;
        List<CardReadDto> allCards = cardService.findAllCardsByClientId(clientId);

        assertThat(allCards.size() > 0).isTrue();
    }

    @Test
    void shouldReturnEmptyList_NotExistClientId_FindAllCardsByClientId_Test() {
        clientId = 10L;
        List<CardReadDto> allCards = cardService.findAllCardsByClientId(clientId);

        assertThat(allCards.size()).isEqualTo(0);
    }
}