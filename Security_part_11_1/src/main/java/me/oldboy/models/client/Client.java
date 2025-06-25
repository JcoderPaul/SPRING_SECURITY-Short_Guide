package me.oldboy.models.client;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.client_info.Contact;
import me.oldboy.models.client_info.Details;
import me.oldboy.models.money.Account;
import me.oldboy.models.money.Card;
import me.oldboy.models.money.Loan;
import me.oldboy.models.money.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "pass")
    private String pass;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "details_id")
    private Details details;

    @OneToOne(mappedBy = "client",
              cascade = CascadeType.PERSIST)
    private Contact contact;

    @OneToOne(mappedBy = "client",
              cascade = CascadeType.PERSIST)
    private Account account;

    @OneToMany(mappedBy = "client",
               cascade = CascadeType.PERSIST)
    @Builder.Default
    List<Transaction> transactionList = new ArrayList<>();

    public void addTransactionToList(Transaction transaction) {
        transactionList.add(transaction);
        transaction.setClient(this);
    }

    @OneToMany(mappedBy = "client",
               cascade = CascadeType.PERSIST)
    @Builder.Default
    List<Card> cardsList = new ArrayList<>();

    public void addCardsToList(Card card) {
        cardsList.add(card);
        card.setClient(this);
    }

    @ManyToMany(fetch = FetchType.EAGER,
                cascade = CascadeType.PERSIST)
    @JoinTable(name = "clients_authorities",
               joinColumns = @JoinColumn(name = "client_id"),
               inverseJoinColumns = @JoinColumn(name = "authority_id"))
    @Builder.Default
    private List<Auth> listOfAuth = new ArrayList<>();

    public void addAuthToList(Auth auth){
        listOfAuth.add(auth);
    }

    /*
    Повторение пройденного:
    Данная сущность "описывает структуру" таблицы clients. Из таблицы loans на таблицу clients у нас есть ссылка
    client_id (loans) -> id (client). Связь в данном случает OneToMany, т.е. один клиент может набрать много
    кредитов. Эту связь мы и должны описать.

    Мы делаем двунаправленную (bidirectional) связь, т.е. в сущности loan прописана обратная аннотация к текущей
    - @ManyToOne. Так же, очень важно, мы создали метод *.addLoanToList(), где не только добавляем loan в коллекцию,
    но и прописываем, в полученный через аргументы loan, текущего client. Т.е. теперь client "помнит" (знает) о всех
    своих кредитах, а также, в каждый кредит взятый текущим клиентом прописываются сведения об нем.

    - ШАГ 1. - Добавляем в текущую сущность список loan-s - один клиент ("One" Client) с множеством кредитов
               ("ToMany" Loan-s).
    - ШАГ 2. - Добавим параметр каскадирования.
    - ШАГ 3. - Указываем связь (mappedBy) - через какое поле текущий класс Client связан с классом Loan. В нашем
               случае - поле в сущности Loan называется client. В свою очередь само поле client, класса Loan имеет
               обратную аннотацию @ManyToOne и соответствующие параметры описывающие связь таблиц через foreign key.

    Связь организована.
    */
    @OneToMany(cascade = CascadeType.PERSIST,
               mappedBy = "client")
    @Builder.Default
    private List<Loan> listOfLoans = new ArrayList<>();

    public void addLoanToList(Loan loan){
        listOfLoans.add(loan);
        loan.setClient(this);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", pass='" + pass + '\'' +
                ", role=" + role +
                ", details=" + details +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id) &&
                Objects.equals(email, client.email) &&
                Objects.equals(pass, client.pass) &&
                role == client.role &&
                Objects.equals(details, client.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, pass, role, details);
    }
}