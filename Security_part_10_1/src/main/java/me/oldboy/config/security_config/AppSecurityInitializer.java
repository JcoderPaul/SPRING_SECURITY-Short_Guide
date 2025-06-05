package me.oldboy.config.security_config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/*
Без данного класса Spring Security (контекст) фильтры не будут запущены, а значит не будет и защиты, см. док:

Регистрирует DelegatingFilterProxy для использования springSecurityFilterChain перед любым другим
зарегистрированным фильтром. При использовании AbstractSecurityWebApplicationInitializer(Class...)
происходит регистрация ContextLoaderListener. Класс расширяющий AbstractSecurityWebApplicationInitializer()
обычно используется в дополнение к подклассу AbstractContextLoaderInitializer.

В предыдущем варианте приложения уже Spring Boot мы явно его не видим и не наследуемся от него - вся
конфигурация состоит из пары файлов.
*/
public class AppSecurityInitializer extends AbstractSecurityWebApplicationInitializer {
}
