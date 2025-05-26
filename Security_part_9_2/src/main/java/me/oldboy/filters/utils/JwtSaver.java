package me.oldboy.filters.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
