package me.oldboy.models.money;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.client.Client;

import java.time.LocalDate;
import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long loanId;

    /*
    Повторим материал:
    Файл DOC/SQL/loans_scripts.sql - скрипт таблицы loans, в файле DOC/SQL/scripts.sql - расположен скрипт
    таблицы clients, эти таблицы связаны через внешний ключ - FOREIGN KEY. И эта связь один-ко-многим
    (многие-к-одному) - т.е. к одному клиенту может быть привязано много кредитов, или множество кредитов
    может набрать один клиент.

    Вот эту связь мы и должны прописать в наших сущностях:

    - ШАГ.1 - В текущей сущности прописываем целую сущность с которой она связанна (в таблице у нас прописан
              лишь ее ID - client_id, см. SQL скрипт).
    - ШАГ.2 - Аннотируем текущее поле, как @ManyToOne - много кредитов (loans) могут быть связанны с одним
              клиентом (client).
    - ШАГ.3 - Тип каскадирования зависит от логики проекта, в нашем случае мы заинтересованы в каскадном
              сохранении данных при создании или изменении.
    - ШАГ.4 - Аннотируем текущее поле, как @JoinColumn и прописываем в параметрах foreign key столбец, который
              ссылается на таблицу clients - у нас это client_id и ссылается он на столбец id из таблицы clients.

    Закрепим повторенное:
    Когда мы работаем с аннотацией @JoinColumn, то в параметрах всегда указываем foreign key столбец из таблицы,
    которая связана с текущей сущностью - в нашем случае таблица loans (сущность loan) и столбец client_id.

    Еще пара понятий:
    В данном случае таблица loans указывает на таблицу clients и называется source-таблицей, а таблица на которую
    она указывает через foreign key называется target-таблицей. Но это не стандарт, т.е. расположить foreign key
    мы можем и в целевой таблице с обратной ссылкой. Аннотация @JoinColumn указывает, например Hibernate-у, где
    искать связь между таблицами (clients и loans).

    Когда речь идет о связи ManyToOne, то foreign key всегда будет находиться в таблице которая отвечает за MANY.
    */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "start_dt")
    private LocalDate startDate;

    @Column(name = "loan_type")
    private String loanType;

    @Column(name = "total_loan")
    private Integer totalLoan;

    @Column(name = "amount_paid")
    private Integer amountPaid;

    @Column(name = "outstanding_amount")
    private Integer outstandingAmount;

    @Column(name = "create_dt")
    private LocalDate createDate;

    @Override
    public String toString() {
        return "Loan{" +
                "loanId=" + loanId +
                ", startDate=" + startDate +
                ", loanType='" + loanType + '\'' +
                ", totalLoan=" + totalLoan +
                ", amountPaid=" + amountPaid +
                ", outstandingAmount=" + outstandingAmount +
                ", createDate=" + createDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(loanId, loan.loanId) &&
                Objects.equals(startDate, loan.startDate) &&
                Objects.equals(loanType, loan.loanType) &&
                Objects.equals(totalLoan, loan.totalLoan) &&
                Objects.equals(amountPaid, loan.amountPaid) &&
                Objects.equals(outstandingAmount, loan.outstandingAmount) &&
                Objects.equals(createDate, loan.createDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId, startDate, loanType, totalLoan, amountPaid, outstandingAmount, createDate);
    }
}
