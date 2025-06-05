package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.mapper.ContactMapper;
import me.oldboy.models.client_info.Contact;
import me.oldboy.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Transactional(readOnly = true)
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    /* Работает общая аннотация над классом @Transactional, тут мы только читаем */
    public Optional<ContactReadDto> readContact(Long clientId) {
        Optional<ContactReadDto> contact = Optional.empty();
        Optional<Contact> mayBeContact = contactRepository.findByClientId(clientId);
        if (mayBeContact.isEmpty()) {
            return contact;
        } else {
            contact = Optional.of(ContactMapper.INSTANCE.mapToContactReadDto(mayBeContact.get()));
            return contact;
        }
    }

    public List<Contact> readAllContacts(){
        return contactRepository.findAll();
    }
}