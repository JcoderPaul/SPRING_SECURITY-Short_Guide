package me.oldboy.mapper;

import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.models.money.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    TransactionReadDto mapToTransactionReadDto(Transaction transaction);
}
