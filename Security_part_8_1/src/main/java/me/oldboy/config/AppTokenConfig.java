package me.oldboy.config;

import lombok.RequiredArgsConstructor;
import me.oldboy.filters.utils.RememberMeUserNameExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class AppTokenConfig {

    private final String KEY = "myKey";

    @Autowired
    private final DataSource dataSource;

    @Autowired
    private final UserDetailsService userDetailsService;

    /*
    Нам нужен bean, который будет взаимодействовать с БД в разрезе хранения remember-me токена.
    Слой repository.
    */
    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    /*
    В прошлых частях мы не переопределяли этот bean, т.к. нам не нужны были его методы явно.
    Так же в более ранних версиях данного проекта мы пытались использовать его для изучения,
    хотя тут он и потерял актуальность - оставим для напоминания.
    Слой - service
    */
    @Bean
    @Primary // У нас тут, как минимум две реализации RememberMeServices и хотя конфликта, скорее всего не будет - я очень хотел применить эту аннотацию
    public RememberMeServices rememberMeServices() {
        PersistentTokenBasedRememberMeServices rememberMeServices =
                new PersistentTokenBasedRememberMeServices(KEY, userDetailsService, tokenRepository());
        return rememberMeServices;
    }

    /*
    Данный бин можно было определить ранее, сделав RememberMeUserNameExtractor компонентом,
    но мы решили реализовать его так, тем более тут проще его настроить - все в одном месте.
    Именно этот bean отвечает за извлечение userName из cookie "remember-me". Хотя мы не
    использовали интерфейс "родителя" в качестве возвращаемого типа, один RememberMeServices
    у нас уже есть - зададим явно имя текущего bean-a.
    */
    @Bean(name = "rememberMeUserNameExtractor")
    public RememberMeUserNameExtractor rememberMeUserNameExtractor(){
        RememberMeUserNameExtractor userNameExtractor =
                new RememberMeUserNameExtractor(KEY, userDetailsService, tokenRepository());
        return userNameExtractor;
    }
}