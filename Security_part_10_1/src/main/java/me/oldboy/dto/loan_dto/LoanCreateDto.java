package me.oldboy.dto.loan_dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class LoanCreateDto {
    private Long clientId;
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
        LoanCreateDto that = (LoanCreateDto) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(loanType, that.loanType) &&
                Objects.equals(totalLoan, that.totalLoan) &&
                Objects.equals(amountPaid, that.amountPaid) &&
                Objects.equals(outstandingAmount, that.outstandingAmount) &&
                Objects.equals(createDate, that.createDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, startDate, loanType, totalLoan, amountPaid, outstandingAmount, createDate);
    }

    @Override
    public String toString() {
        return "Кредит -" +
                ", user ID: " + clientId +
                ", start date: " + startDate +
                ", type: " + loanType +
                ", total loan: " + totalLoan +
                ", amount paid: " + amountPaid +
                ", outstanding amount: " + outstandingAmount +
                ", create date: " + createDate;
    }

    @JsonProperty("clientId")
    public Long getClientId() {
        return clientId;
    }

    @JsonProperty("startDate")
    public LocalDate getStartDate() {
        return startDate;
    }

    @JsonProperty("loanType")
    public String getLoanType() {
        return loanType;
    }

    @JsonProperty("totalLoan")
    public Integer getTotalLoan() {
        return totalLoan;
    }

    @JsonProperty("amountPaid")
    public Integer getAmountPaid() {
        return amountPaid;
    }

    @JsonProperty("outstandingAmount")
    public Integer getOutstandingAmount() {
        return outstandingAmount;
    }

    @JsonProperty("createDate")
    public LocalDate getCreateDate() {
        return createDate;
    }
}