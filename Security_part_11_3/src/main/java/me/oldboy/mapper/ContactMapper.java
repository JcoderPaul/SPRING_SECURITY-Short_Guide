package me.oldboy.mapper;

import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.models.client_info.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContactMapper {

    ContactMapper INSTANCE = Mappers.getMapper(ContactMapper.class);

    ContactReadDto mapToContactReadDto(Contact contact);
}
