package me.oldboy.unit.services;

import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.models.client_info.Contact;
import me.oldboy.repository.ContactRepository;
import me.oldboy.services.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository mockContactRepository;
    @InjectMocks
    private ContactService contactService;

    private Contact testContact;
    private ContactReadDto testContactReadDto;
    private Long testId;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testId = 1L;

        testContact = Contact.builder()
                .city("Gondor")
                .address("Romashka st.")
                .building(1423)
                .apartment(332)
                .build();
        testContactReadDto = ContactReadDto.builder()
                .city("Gondor")
                .address("Romashka st.")
                .building(1423)
                .apartment(332)
                .build();
    }

    @Test
    void shouldReturn_EqDto_ReadContact_Test() {
        when(mockContactRepository.findByClientId(testId)).thenReturn(Optional.of(testContact));

        Optional<ContactReadDto> mayBeDto = contactService.readContact(testId);
        if(mayBeDto.isPresent()){
            assertAll(
                    () -> assertThat(mayBeDto.get().getCity()).isEqualTo(testContactReadDto.getCity()),
                    () -> assertThat(mayBeDto.get().getAddress()).isEqualTo(testContactReadDto.getAddress()),
                    () -> assertThat(mayBeDto.get().getBuilding()).isEqualTo(testContactReadDto.getBuilding()),
                    () -> assertThat(mayBeDto.get().getApartment()).isEqualTo(testContactReadDto.getApartment())
            );
        }

        verify(mockContactRepository, times(1)).findByClientId(testId);
    }

    @Test
    void shouldReturn_EmptyOptional_ReadContact_Test() {
        when(mockContactRepository.findByClientId(testId)).thenReturn(Optional.empty());

        assertThat(contactService.readContact(testId).isEmpty()).isTrue();

        verify(mockContactRepository, times(1)).findByClientId(testId);
    }
}