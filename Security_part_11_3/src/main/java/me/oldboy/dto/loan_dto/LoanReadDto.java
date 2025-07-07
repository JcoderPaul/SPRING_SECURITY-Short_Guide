package me.oldboy.dto.loan_dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LoanReadDto {
    private LocalDate startDate;
    private String loanType;
    private Integer totalLoan;
    private Integer amountPaid;
    private Integer outstandingAmount;
    private LocalDate createDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanReadDto that = (LoanReadDto) o;
        return Objects.equals(startDate, that.startDate) &&
                Objects.equals(loanType, that.loanType) &&
                Objects.equals(totalLoan, that.totalLoan) &&
                Objects.equals(amountPaid, that.amountPaid) &&
                Objects.equals(outstandingAmount, that.outstandingAmount) &&
                Objects.equals(createDate, that.createDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, loanType, totalLoan, amountPaid, outstandingAmount, createDate);
    }

    @Override
    public String toString() {
        return "Кредит -" +
                " start date: " + startDate +
                ", type: " + loanType +
                ", total loan: " + totalLoan +
                ", amount paid: " + amountPaid +
                ", outstanding amount: " + outstandingAmount +
                ", create date: " + createDate;
    }
}
