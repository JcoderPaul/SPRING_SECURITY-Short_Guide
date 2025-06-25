package me.oldboy.models.money;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.client.Client;

import java.time.LocalDate;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "card_number")
    private String cardNumber;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "total_limit")
    private Integer totalLimit;

    @Column(name = "amount_used")
    private Integer amountUsed;

    @Column(name = "available_amount")
    private Integer availableAmount;

    @Column(name = "create_dt")
    private LocalDate createDt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(cardId, card.cardId) &&
                Objects.equals(cardNumber, card.cardNumber) &&
                Objects.equals(cardType, card.cardType) &&
                Objects.equals(totalLimit, card.totalLimit) &&
                Objects.equals(amountUsed, card.amountUsed) &&
                Objects.equals(availableAmount, card.availableAmount) &&
                Objects.equals(createDt, card.createDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, cardNumber, cardType, totalLimit, amountUsed, availableAmount, createDt);
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardId=" + cardId +
                ", cardNumber='" + cardNumber + '\'' +
                ", cardType='" + cardType + '\'' +
                ", totalLimit=" + totalLimit +
                ", amountUsed=" + amountUsed +
                ", availableAmount=" + availableAmount +
                ", createDt=" + createDt +
                '}';
    }
}
