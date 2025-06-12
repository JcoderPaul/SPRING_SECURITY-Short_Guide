package me.oldboy.integration.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.services.ContactService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class ContactServiceTestIT extends IntegrationTestBase {

    private final ContactService contactService;

    private Long clientId;

    @Test
    void shouldReturnTrue_OptionalContactReadDto_ReadContact_Test() {
        clientId = 1L;
        Optional<ContactReadDto> findContact = contactService.readContact(clientId);

        assertThat(findContact.isPresent()).isTrue();
    }

    @Test
    void shouldReturnOptionalEmpty_NotExistClient_ReadContact_Test() {
        clientId = 10L;
        Optional<ContactReadDto> findContact = contactService.readContact(clientId);

        assertThat(findContact.isEmpty()).isTrue();
    }
}