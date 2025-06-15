package me.oldboy.unit.services;

import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.models.client_info.Contact;
import me.oldboy.repository.ContactRepository;
import me.oldboy.services.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

class ContactServiceTest {

    @Mock
    private ContactRepository mockContactRepository;
    @InjectMocks
    private ContactService contactService;

    private Contact testContact;
    private Long testId;
    private String testAddress, testCity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testId = 1L;

        testAddress = "Merlin st.";
        testCity = "Avalon";

        testContact = Contact.builder()
                .id(testId)
                .address(testAddress)
                .city(testCity)
                .build();
    }

    @Test
    void shouldReturnContactEqualContactReadDto_ReadContact_Test() {
        when(mockContactRepository.findByClientId(testId)).thenReturn(Optional.of(testContact));

        var originalContactDto = contactService.readContact(testId);

        assertAll(
                () -> assertThat(originalContactDto.get().getAddress()).isEqualTo(testContact.getAddress()),
                () -> assertThat(originalContactDto.get().getCity()).isEqualTo(testContact.getCity())
        );
    }

    @Test
    void shouldReturnEmptyDto_ReadContact_Test() {
        when(mockContactRepository.findByClientId(testId)).thenReturn(Optional.empty());

        var expectedEmpty = contactService.readContact(testId);

        assertThat(Optional.empty()).isEqualTo(expectedEmpty);
    }

    @Test
    void shouldReturnContactReadDto_ReadContact_Test() {
        when(mockContactRepository.findByClientId(testId)).thenReturn(Optional.of(testContact));
        assertThat(contactService.readContact(testId).get() instanceof ContactReadDto).isTrue();
    }
}