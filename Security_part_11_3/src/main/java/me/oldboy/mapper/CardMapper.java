package me.oldboy.mapper;

import me.oldboy.dto.card_dto.CardReadDto;
import me.oldboy.models.money.Card;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    CardReadDto mapToTransactionReadDto(Card card);
}
