package me.oldboy.dto.account_dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AccountReadDto {
    private Long accountNumber;
    private String accountType;
    private String branchAddress;
    private LocalDate createDt;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountReadDto that = (AccountReadDto) o;
        return Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(accountType, that.accountType) &&
                Objects.equals(branchAddress, that.branchAddress) &&
                Objects.equals(createDt, that.createDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, accountType, branchAddress, createDt);
    }

    @Override
    public String toString() {
        return "Account number: " + accountNumber +
                ", Account type: " + accountType +
                ", Branch address: " + branchAddress +
                ", Create date: " + createDt;
    }
}