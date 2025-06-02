package me.oldboy.config.test_main_config;

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
        ,"me.oldboy.config.test_data_source"
        ,"me.oldboy.config.test_security_config"
        ,"me.oldboy.config.security_details"
})
public class TestMainConfig {
}
