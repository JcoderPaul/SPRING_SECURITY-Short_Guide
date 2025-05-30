package me.oldboy.repository;

import me.oldboy.models.client_info.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /*
    Обычно в Spring приложениях используются встроенные реализации методов репозиториев, подразумевается, что
    существует некая стандартизация при работе с БД и при именовании ее полей, а значит могут быть уже готовые
    реализации методов. И тогда сам метод можно даже не упоминать в своем репозитории если он унаследован от
    JpaRepository. Но если так хочется, то можно и явно прописать его, но без реализации в дальнейшем наследнике.
    Тогда метод будет выглядеть примерно так (главное правильно составить название), получается коротко и просто:

    Optional<Contact> findByClientId(Long clientId);

    Однако существуют ситуации, когда все не стандартно и тогда мы можем применить нативный запрос в котором
    описываем его SQL синтаксис и далее в методе явно именуем передаваемые в него параметры, см. ниже:
    */

    @Query(value = "SELECT cont.* " +
                   "FROM client_contacts AS cont " +
                   "WHERE cont.client_id = :clientId",
           nativeQuery = true)
    Optional<Contact> findByClientId(@Param("clientId") Long clientId);
}
