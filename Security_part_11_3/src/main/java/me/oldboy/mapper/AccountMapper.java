package me.oldboy.mapper;

import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.models.money.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountReadDto mapToAccountReadDto(Account account);
}
