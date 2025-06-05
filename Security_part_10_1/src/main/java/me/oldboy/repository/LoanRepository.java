package me.oldboy.repository;

import me.oldboy.models.money.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>,
                                        MyLoanRepository {

    /*
    Можно название метода написать сообразно правилам составления имен для подобного типа методов,
    чтобы воспользоваться удобствами встроенной реализации из Spring-a для JpaRepository, а можно
    применить т.н. нативные запросы, как показано ниже. При этом в SpringBoot, обычно достаточно
    указать параметр передаваемый в метод, но в nonBoot версии приложения можно поймать исключение:

    "For queries with named parameters you need to provide names for method parameters; Use @Param
    for query method parameters, or when on Java 8+ use the javac flag -parameters"

    Поэтому, как и рекомендовано, мы применяем явно именованный параметр, см. ниже., можно сравнить
    с предыдущими версиями данного метода в других разделах (примерах приложения).
    */
    @Query(value = "SELECT * " +
                   "FROM loans " +
                   "WHERE loans.client_id = :clientId",
           nativeQuery = true)
    Optional<List<Loan>> findAllByClientId(@Param("clientId") Long clientId);
}
