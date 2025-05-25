package me.oldboy.filters.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*
Повторим, идея работы с JWT token-ом жестко разделяет стороны клиента и приложения, т.е. приложение (сервер, сервис) только генерирует токен,
а клиент (пользователь) хранит и передает его при запросе "услуг". Второй функцией сервера (сервиса, приложения) от которого хотят получить
услугу (данные) будет проверка (валидация) полученного с запросом токена. При этом данный способ аутентификации хорош для REST приложений
(сервисов) - запрос на аутентификацию к приложению, возвращение token-a в ответ при подтверждении введенных данных (успешной аутентификации).

Но мы решили исследовать работу этой схемы в монолитном проекте - где мы, как бы объединяем функции front-end и back-end частей приложения,
т.е. нарушение самой идеи, а значит как-то должны получать и хранить сведения, и о token-е, и о том для кого его генерировали.

Данный класс этим и занимается.
*/
@Component
public class JwtSaver {

        private Map<String, String> authMap = new HashMap<>();

        public void saveJwtToken(String keyEmail, String jwtFromAttribute) {
            authMap.put(keyEmail, jwtFromAttribute);
        }

        public String getSavedJwt(String emailKeyToGet){
            return authMap.get(emailKeyToGet);
        }

        public void setJwtToNull(String emailKeyToDelete) {
            authMap.remove(emailKeyToDelete);
        }
}
