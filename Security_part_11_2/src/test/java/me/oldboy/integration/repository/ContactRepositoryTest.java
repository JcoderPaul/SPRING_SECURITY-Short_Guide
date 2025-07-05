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
class ContactRepositoryTest extends IntegrationTestBase {

    private final ContactRepository contactRepository;

    private Long userId;

    @Test
    @DisplayName("Test 1: Find contact data by client ID from DB")
    @Order(1)
    @Rollback(value = false)
    void ShouldReturnTrueIfFindContactByClientIdTest() {
        userId = 1L;
        Optional<Contact> mayBeContact = contactRepository.findByClientId(userId);
        System.out.println(mayBeContact);

        assertThat(mayBeContact.isPresent()).isTrue();
    }
}