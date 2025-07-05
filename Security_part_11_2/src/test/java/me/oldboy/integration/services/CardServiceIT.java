package me.oldboy.integration.services;

import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.services.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardServiceIT extends IntegrationTestBase {

    @Autowired
    private CardService cardService;

    private Long clientId;

    @Test
    void findAllCardsByClientId_ShouldReturnDtoList_Test() {
        clientId = 1L;
        List<CardReadDto> allCards = cardService.findAllCardsByClientId(clientId);

        assertThat(allCards.size() > 0).isTrue();
    }

    @Test
    void findAllCardsByClientId_ShouldReturnEmptyList_NotExistClientId_Test() {
        clientId = 10L;
        List<CardReadDto> allCards = cardService.findAllCardsByClientId(clientId);

        assertThat(allCards.size()).isEqualTo(0);
    }
}