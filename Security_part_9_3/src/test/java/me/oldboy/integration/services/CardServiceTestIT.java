package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class CardServiceTestIT extends TestContainerInit {

    @Autowired
    private CardService cardService;

    private Long testExistId, testNotExistId;

    @BeforeEach
    void setUp(){
        testExistId = 1L;
        testNotExistId = 100L;
    }

    @Test
    void findAllCardsByClientId_ShouldReturnCardList_ForExistingClientWithCards_Test() {
        List<CardReadDto> fromBaseList = cardService.findAllCardsByClientId(testExistId);
        assertThat(fromBaseList.size()).isGreaterThan(1);
        assertThat(fromBaseList.get(0).getCardType()).isEqualTo("Credit");
    }

    @Test
    void findAllCardsByClientId_ShouldReturnEmptyList_ForClientWithoutCards_Test() {
        List<CardReadDto> fromBaseList = cardService.findAllCardsByClientId(testNotExistId);
        assertThat(fromBaseList.size()).isEqualTo(0);
    }
}