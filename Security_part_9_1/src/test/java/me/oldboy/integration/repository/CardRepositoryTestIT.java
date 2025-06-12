package me.oldboy.integration.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.money.Card;
import me.oldboy.repository.CardRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class CardRepositoryTestIT extends IntegrationTestBase {

    private final CardRepository cardRepository;

    private Long clientId;

    @Test
    @DisplayName("Test 7: Find cards by client ID from DB")
    @Order(7)
    @Rollback(value = false)
    void shouldReturnTrue_CardList_IfFindByClientId_Test() {
        clientId = 1L;
        Optional<List<Card>> mayBeCards = cardRepository.findByClientId(clientId);

        assertThat(mayBeCards.isPresent()).isTrue();
        assertThat(mayBeCards.get().size() > 0).isTrue();
    }

    @Test
    @DisplayName("Test 8: Can not find cards by client ID")
    @Order(8)
    @Rollback(value = false)
    void shouldReturnEmptyList_IfNotFindCardsByClientId_Test() {
        clientId = 10L;
        Optional<List<Card>> mayBeCards = cardRepository.findByClientId(clientId);

        assertThat(mayBeCards.get().size()).isEqualTo(0);
    }
}