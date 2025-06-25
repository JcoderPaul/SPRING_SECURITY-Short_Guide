package me.oldboy.dto.card_dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CardReadDto {
    private String cardNumber;
    private String cardType;
    private Integer totalLimit;
    private Integer amountUsed;
    private Integer availableAmount;
    private LocalDate createDt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardReadDto that = (CardReadDto) o;
        return Objects.equals(cardNumber, that.cardNumber) && Objects.equals(cardType, that.cardType) && Objects.equals(totalLimit, that.totalLimit) && Objects.equals(amountUsed, that.amountUsed) && Objects.equals(availableAmount, that.availableAmount) && Objects.equals(createDt, that.createDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, cardType, totalLimit, amountUsed, availableAmount, createDt);
    }

    @Override
    public String toString() {
        return "Card number: " + cardNumber +
                ", card type: " + cardType +
                ", total limit: " + totalLimit +
                ", amount used: " + amountUsed +
                ", available amount: " + availableAmount +
                ", create date: " + createDt;
    }
}
