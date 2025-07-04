package me.oldboy.repository;

import me.oldboy.models.money.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>,
                                        MyLoanRepository {

    @Query(value = "SELECT * " +
                   "FROM loans " +
                   "WHERE loans.client_id = :clientId",
           nativeQuery = true)
    Optional<List<Loan>> findAllByClientId(Long clientId);
}
