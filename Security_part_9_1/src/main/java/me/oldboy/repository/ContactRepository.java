package me.oldboy.repository;

import me.oldboy.models.client_info.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    @Query(value = "SELECT cont.* " +
                   "FROM client_contacts AS cont " +
                   "WHERE cont.client_id = :clientId",
           nativeQuery = true)
    Optional<Contact> findByClientId(Long clientId);
}
