package me.oldboy.integration.services;

import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.services.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ContactServiceIT extends IntegrationTestBase {

    @Autowired
    private ContactService contactService;

    private Long clientId;

    @Test
    void readContact_ShouldReturnTrue_AndOptionalContactReadDto_Test() {
        clientId = 1L;
        Optional<ContactReadDto> findContact = contactService.readContact(clientId);

        assertThat(findContact.isPresent()).isTrue();
    }

    @Test
    void readContact_ShouldReturnOptionalEmpty_ForNotExistClient_Test() {
        clientId = 10L;
        Optional<ContactReadDto> findContact = contactService.readContact(clientId);

        assertThat(findContact.isEmpty()).isTrue();
    }
}