package me.oldboy.repository;

import me.oldboy.models.money.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query(value = "SELECT * " +
                   "FROM cards " +
                   "WHERE cards.client_id = :clientId",
            nativeQuery = true)
    Optional<List<Card>> findByClientId(Long clientId);
}
