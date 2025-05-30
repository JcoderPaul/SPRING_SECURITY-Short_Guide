package me.oldboy.models.money;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.client.Client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number")
    private Long accountNumber;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "account_type")
    private String accountType;
    @Column(name = "branch_address")
    private String branchAddress;
    @Column(name = "create_dt")
    private LocalDate createDt;

    @OneToMany(mappedBy = "account",
               cascade = CascadeType.PERSIST)
    @Builder.Default
    List<Transaction> transactionList = new ArrayList<>();

    public void addTransactionToList(Transaction transaction) {
        transactionList.add(transaction);
        transaction.setAccount(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountId, account.accountId) &&
                Objects.equals(accountNumber, account.accountNumber) &&
                Objects.equals(accountType, account.accountType) &&
                Objects.equals(branchAddress, account.branchAddress) &&
                Objects.equals(createDt, account.createDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, accountNumber, accountType, branchAddress, createDt);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", accountNumber=" + accountNumber +
                ", accountType='" + accountType + '\'' +
                ", branchAddress='" + branchAddress + '\'' +
                ", createDt=" + createDt +
                '}';
    }
}
