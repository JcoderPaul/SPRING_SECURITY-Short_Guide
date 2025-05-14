package me.oldboy.integration.annotation;

import me.oldboy.integration.TestAppRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@Transactional
@Sql({"classpath:sql_scripts/data.sql"})    // Создадим и заполним таблицы БД
@SpringBootTest(classes = TestAppRunner.class)
public @interface IT {
}
