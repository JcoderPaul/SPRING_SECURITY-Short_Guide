package me.oldboy.repository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.models.PrintAuthorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_SQL = "SELECT * FROM authorities";

    public List<PrintAuthorities> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, roleRowMapper);
    }

    private RowMapper<PrintAuthorities> roleRowMapper = (row, rowNumber) ->
            new PrintAuthorities(row.getString("username"),
                                 row.getString("authority"));
}
