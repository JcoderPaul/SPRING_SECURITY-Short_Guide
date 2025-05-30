package me.oldboy.repository;

import me.oldboy.models.money.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Transaction, Long> {

    Optional<List<Transaction>> findByClientId(Long clientId);

    @Query(value = "SELECT at.* " +
                   "FROM account_transactions AS at " +
                   "WHERE at.account_number = :accountNumber",
            nativeQuery = true)
    Optional<List<Transaction>> findByAccountNumber(Long accountNumber);
}
