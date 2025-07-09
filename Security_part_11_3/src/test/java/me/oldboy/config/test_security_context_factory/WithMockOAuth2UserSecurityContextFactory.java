package me.oldboy.config.test_security_context_factory;

import me.oldboy.integration.annotation.WithMockOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.*;
import java.util.stream.Collectors;


public class WithMockOAuth2UserSecurityContextFactory implements WithSecurityContextFactory<WithMockOAuth2User> {

    @Override
    public SecurityContext createSecurityContext(WithMockOAuth2User mockOAuth2User) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", mockOAuth2User.username());
        attributes.put("email", mockOAuth2User.email());

        Set<String> authorities = Arrays.stream(mockOAuth2User.authorities()).collect(Collectors.toSet());
        OAuth2User principal = new DefaultOAuth2User(Collections.emptySet(), attributes, "username");

        Authentication authentication = new OAuth2AuthenticationToken(principal, authorities.stream()
                .map(a -> (org.springframework.security.core.GrantedAuthority) () -> a)
                .collect(Collectors.toSet()), "KeyCloak");

        context.setAuthentication(authentication);
        return context;
    }
}
