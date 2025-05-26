package me.oldboy.mapper;

import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.models.money.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanMapper {

    LoanMapper INSTANCE = Mappers.getMapper(LoanMapper.class);

    LoanReadDto mapToLoanReadDto(Loan loan);
}
