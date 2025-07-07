package me.oldboy.config.main_config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "me.oldboy.config.auth_event_listener"
        ,"me.oldboy.config.data_source"
        ,"me.oldboy.config.jwt_pwe_config"
        ,"me.oldboy.config.security_config"
        ,"me.oldboy.config.security_details"
        ,"me.oldboy.controllers"
        ,"me.oldboy.services"
        ,"me.oldboy.repository"
        ,"me.oldboy.exception"
})
public class MainAppConfig {

}
