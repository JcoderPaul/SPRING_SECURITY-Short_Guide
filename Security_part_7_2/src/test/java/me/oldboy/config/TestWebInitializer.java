package me.oldboy.config;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.test_main_config.TestMainConfig;
import me.oldboy.config.test_security_config.TestSecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

@Slf4j
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan({
        "me.oldboy.controllers"
        ,"me.oldboy.exception"
})
public class TestWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
