package me.oldboy.filters.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.*;

import java.util.Optional;

/*
И так, мы хотим из cookie "remember-me" запроса, если он есть, извлечь userName некогда залогиненного клиента
нашего сервиса, просто, чтобы отобразить его в логах приложения применив *.addFilterBefore() метод в filterChain,
см. AppSecurityConfig.java

Абстрактный класс AbstractRememberMeServices и другие его наследники очень подходят для решения нашей задачи.
Честно говоря, в стандартном режиме они практически этим и занимаются, но с уклоном в безопасность всего
приложения и нам слегка не подходят. Но, это значит, что вышеупомянутые классы содержат готовые методы, и
методы части которых нам очень пригодятся для реализаций нашей задачи. Вариантов два, полностью скопировать
нужный код и некоторые его "огрызки" или унаследоваться от этого сервиса и немного его "подрихтовать".

Наследуем и рихтуем! Не реализуем (оставляем by default) два его требуемых метода - они нам не нужны. Пишем
свой для извлечения имени из Remember-Me token-a. Мы могли бы тут сделать данный класс компонентом Spring-a,
но реализуем это немного иначе, см. AppTokenConfig.java данного проекта.
*/
@Slf4j
public class RememberMeUserNameExtractor extends AbstractRememberMeServices {

    /* Нам понадобиться уже существующий PersistentTokenRepository см. AppTokenConfig.java */
    private final PersistentTokenRepository tokenRepository;

    /* Внедряем все через конструктор */
    public RememberMeUserNameExtractor(String key,
                                       UserDetailsService userDetailsService,
                                       PersistentTokenRepository persistentTokenRepository) {
        super(key, userDetailsService);
        this.tokenRepository = persistentTokenRepository;
    }

    /* Реализуем метод согласно задаче - просто достать имя пользователя из БД по token-у, минимум проверок - код учебный */
    public Optional<String> getUserNameFromToken(HttpServletRequest request){
        /*
        Извлекаем remember-me cookies из запроса, именно из запроса - нам интересно, если таковой в нем, см.
        реализацию в AbstractRememberMeServices.java на https://github.com/spring-projects/spring-security
        */
        String rememberMeCookie = extractRememberMeCookie(request);
        if (rememberMeCookie == null || rememberMeCookie.length() == 0) {
            log.info("Token was empty!");
            return Optional.empty();
        } else {
            /* Расшифровываем полученный remember-me cookies из запроса, см. реализацию в AbstractRememberMeServices.java */
            String[] cookieTokens = decodeCookie(rememberMeCookie);
            if (cookieTokens.length != 2) {
                log.info("Cookie not valid to working with it!");
                /* Тут можно бросить исключение, как в методе *.processAutoLoginCookie() класса PersistentTokenBasedRememberMeServices */
                return Optional.empty();
            }
            /*
            В базе данных есть таблица persistent_logins (см. DOC/SQL/persistent_logins_token.sql текущего проекта),
            в данной таблице содержится поле series, по которому мы можем из БД извлечь remember-me токен, а уже из
            него извлечь требуемое нам имя. Этим и занимаются три строки кода ниже.
            */
            String presentedSeries = cookieTokens[0];
            PersistentRememberMeToken token = this.tokenRepository.getTokenForSeries(presentedSeries);
            return Optional.of(token.getUsername());
        }
    }

    @Override
    protected void onLoginSuccess(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Authentication successfulAuthentication) {
        /* We don't need to implement this method. */
    }

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws RememberMeAuthenticationException, UsernameNotFoundException {
        /* We don't need to implement this method. */
        return null;
    }
}