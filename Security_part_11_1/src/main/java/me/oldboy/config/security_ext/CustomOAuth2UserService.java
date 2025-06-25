package me.oldboy.config.security_ext;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.config.AppSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

        @Autowired
        private UserDetailsService userDetailsService;

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest)  {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) super.loadUser(userRequest);

        String email = (String) oAuth2User.getAttributes().get("blog");
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2User oAuth2UserForProxy = new DefaultOAuth2User(userDetails.getAuthorities(), attributes,"blog");
        Set<Method> userDetailsMethods = Set.of(UserDetails.class.getMethods());

        return (OAuth2User) Proxy.newProxyInstance(AppSecurityConfig.class.getClassLoader(),
                new Class[]{UserDetails.class, OAuth2User.class},
                (proxy, method, args) -> userDetailsMethods.contains(method)
                        ? method.invoke(userDetails, args)
                        : method.invoke(oAuth2UserForProxy, args));
        }
}
