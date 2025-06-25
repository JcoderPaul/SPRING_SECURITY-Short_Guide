package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.client_info.Contact;
import me.oldboy.services.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@IT
class ContactServiceIT extends TestContainerInit {

    @Autowired
    private ContactService contactService;

    private Long testExistId, testNotExistId;

    @BeforeEach
    void setUp(){
        testExistId = 1L;
        testNotExistId = 100L;
    }

    @Test
    void readContact_ShouldReturnCorrectData_ForExistClientId_Test() {
        Optional<ContactReadDto> mayBeDtoFromBase = contactService.readContact(testExistId);
        mayBeDtoFromBase.ifPresent(contactReadDto -> assertAll(
                () -> assertThat(contactReadDto.getCity()).isNotNull(),
                () -> assertThat(contactReadDto.getApartment()).isGreaterThan(0),
                () -> assertThat(contactReadDto.getBuilding()).isGreaterThan(0),
                () -> assertThat(contactReadDto.getAddress()).isNotNull()
        ));
    }

    @Test
    void readContact_ShouldReturnOptionalEmpty_NotExistId_Test() {
        Optional<ContactReadDto> mayBeDtoFromBase = contactService.readContact(testNotExistId);
        assertThat(mayBeDtoFromBase.isEmpty()).isTrue();
    }

    /* Нам известно, что в БД 5-ть записей о контактах */
    @Test
    void readAllContacts_ShouldReturnKnownListSize_Test() {
        List<Contact> allContactListFromBase = contactService.readAllContacts();
        assertThat(allContactListFromBase.size()).isEqualTo(5);
    }
}