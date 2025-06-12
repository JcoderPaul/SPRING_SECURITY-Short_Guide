package me.oldboy.integration.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client_info.Contact;
import me.oldboy.repository.ContactRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class ContactRepositoryTestIT extends IntegrationTestBase {

    private final ContactRepository contactRepository;

    private Long userId;

    @Test
    @DisplayName("Test 1: Find contact data by client ID from DB")
    @Order(1)
    @Rollback(value = false)
    void shouldReturnTrue_IfFindContactByClientId_Test() {
        userId = 1L;
        Optional<Contact> mayBeContact = contactRepository.findByClientId(userId);

        assertThat(mayBeContact.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Test 2: Can not find contact data by client ID from DB")
    @Order(2)
    @Rollback(value = false)
    void shouldReturnFalse_IfNotFindContactByClientId_Test() {
        userId = 10L;
        Optional<Contact> mayBeContact = contactRepository.findByClientId(userId);

        assertThat(mayBeContact.isPresent()).isFalse();
    }
}