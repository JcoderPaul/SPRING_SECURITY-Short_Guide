package me.oldboy.config.main_config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "me.oldboy.controllers"
        ,"me.oldboy.services"
        ,"me.oldboy.repository"
        ,"me.oldboy.exception"
        ,"me.oldboy.config.auth_event_listener"
        ,"me.oldboy.config.auth_view_init"
        ,"me.oldboy.config.data_source"
        ,"me.oldboy.config.security_config"
        ,"me.oldboy.config.security_details"
})
public class MainAppConfig {
}
