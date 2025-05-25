package me.oldboy.dto.transaction_dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TransactionReadDto {
    private LocalDate transactionDt;
    private String transactionSummary;
    private String transactionType;
    private Integer transactionAmt;
    private Integer closingBalance;
    private LocalDate createDt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionReadDto that = (TransactionReadDto) o;
        return Objects.equals(transactionDt, that.transactionDt) &&
                Objects.equals(transactionSummary, that.transactionSummary) &&
                Objects.equals(transactionType, that.transactionType) &&
                Objects.equals(transactionAmt, that.transactionAmt) &&
                Objects.equals(closingBalance, that.closingBalance) &&
                Objects.equals(createDt, that.createDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionDt, transactionSummary, transactionType, transactionAmt, closingBalance, createDt);
    }

    @Override
    public String toString() {
        return  "Transaction date: " + transactionDt +
                ", Transaction summary: " + transactionSummary +
                ", Transaction type: " + transactionType +
                ", Transaction amount: " + transactionAmt +
                ", Closing balance: " + closingBalance +
                ", Create date: " + createDt;
    }
}
