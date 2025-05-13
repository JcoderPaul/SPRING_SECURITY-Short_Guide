package me.oldboy.mapper;

import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.models.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "clientName", source = "client.details.clientName")
    @Mapping(target = "clientSurName", source = "client.details.clientSurName")
    @Mapping(target = "age", source = "client.details.age")
    ClientReadDto mapToClientReadDto(Client client);

    @Mapping(target = "details", source = "details")
    Client mapToClient(ClientCreateDto clientCreateDto);
}
