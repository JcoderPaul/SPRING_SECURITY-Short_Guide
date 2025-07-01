package me.oldboy.integration.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client_info.Contact;
import me.oldboy.repository.ContactRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class ContactRepositoryIT extends IntegrationTestBase {

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

        assertAll(
                () -> assertThat(mayBeContact.get().getClient()).isNotNull(),
                () -> assertThat(mayBeContact.get().getPostalCode()).isNotNull(),
                () -> assertThat(mayBeContact.get().getCity()).isNotNull(),
                () -> assertThat(mayBeContact.get().getAddress()).isNotNull(),
                () -> assertThat(mayBeContact.get().getBuilding()).isNotNull(),
                () -> assertThat(mayBeContact.get().getApartment()).isNotNull(),
                () -> assertThat(mayBeContact.get().getHomePhone()).isNotNull(),
                () -> assertThat(mayBeContact.get().getMobilePhone()).isNotNull()
        );
    }
}