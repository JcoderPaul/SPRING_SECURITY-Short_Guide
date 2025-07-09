package me.oldboy.integration.annotation;

import me.oldboy.config.test_security_context_factory.WithMockOAuth2UserSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockOAuth2UserSecurityContextFactory.class)
public @interface WithMockOAuth2User {
    String username() default "testuser";
    String email() default "test@example.com";
    String[] authorities() default {"SCOPE_profile", "SCOPE_email"};
}
