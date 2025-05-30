package me.oldboy.config;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.security_config.AppSecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

@Slf4j
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "me.oldboy")
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{AppSecurityConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebAppInitializer.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
